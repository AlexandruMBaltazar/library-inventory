package com.learnkafka.consumer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.entity.Book;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventType;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.producer.DeadLetterQueueProducer;
import com.learnkafka.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"avro-library-tp", "library-events-dlq"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://localhost:8081",
        "spring.kafka.consumer.properties.schema.registry.url=mock://localhost:8081"
})
public class LibraryEventsConsumerIT {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<Long, com.learnkafka.model.LibraryEvent> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private LibraryEventsRepository libraryEventsRepository;

    @SpyBean
    private LibraryEventsConsumer libraryEventsConsumerSpy;

    @SpyBean
    private LibraryEventsService libraryEventsServiceSpy;

    @SpyBean
    private DeadLetterQueueProducer deadLetterQueueProducer;

    @BeforeEach
    void setUp() {
        waitForKafkaListenerContainers();
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    /*
        Make sure that the container that we have "LibraryEventsConsumer" is going to wait until all partitions
        are assigned to it
     */
    private void waitForKafkaListenerContainers() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void shouldPublishNewLibraryEvent() throws ExecutionException, InterruptedException {
        // Given
        com.learnkafka.model.Book expectedBook = createBook(123L, "Test Author", "Test Name");
        com.learnkafka.model.LibraryEvent expectedLibraryEvent = createLibraryEvent(null, com.learnkafka.model.LibraryEventType.NEW, expectedBook);

        // When
        kafkaTemplate.send(configProperties.getLibraryTopic(), expectedLibraryEvent).get();

        // Then
        /*
            Consumer runs in a total different thread than the actual application thread
            It might take a while for the consumer to read the record and process it
         */
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verifyLibraryEventProcessed(expectedLibraryEvent, expectedBook));
    }

    @Test
    void shouldPublishUpdateLibraryEvent() throws ExecutionException, InterruptedException {
        // Given
        Book book = Book.builder()
                .id(123L)
                .author("Test Author")
                .name("Test Name")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();
        book.setLibraryEvent(libraryEvent);

        libraryEventsRepository.save(libraryEvent);

        com.learnkafka.model.Book updatedBook = createBook(123L, "Updated Author", "Updated Name");
        com.learnkafka.model.LibraryEvent updatedLibraryEvent = createLibraryEvent(libraryEvent.getLibraryEventId(), com.learnkafka.model.LibraryEventType.UPDATE, updatedBook);

        // When
        kafkaTemplate.send(configProperties.getLibraryTopic(), updatedLibraryEvent).get();

        // Then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verifyLibraryEventProcessed(updatedLibraryEvent, updatedBook);
            verifyUpdatedLibraryEvent(libraryEvent, updatedBook);
        });
    }

    @Test
    void publishUpdateLibraryEventWithNullLibraryEventIdIsSentToDLQ() throws ExecutionException, InterruptedException {
        com.learnkafka.model.Book updatedBook = createBook(123L, "Updated Author", "Updated Name");
        com.learnkafka.model.LibraryEvent updatedLibraryEvent = createLibraryEvent(null , com.learnkafka.model.LibraryEventType.UPDATE, updatedBook);

        // When
        kafkaTemplate.send(configProperties.getLibraryTopic(), updatedLibraryEvent).get();

        // Then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> verify(deadLetterQueueProducer, times(1))
                .send(isA(ConsumerRecord.class)));
    }

    private com.learnkafka.model.Book createBook(Long id, String author, String name) {
        return com.learnkafka.model.Book.newBuilder()
                .setId(id)
                .setAuthor(author)
                .setName(name)
                .build();
    }

    private com.learnkafka.model.LibraryEvent createLibraryEvent(Long libraryEventId, com.learnkafka.model.LibraryEventType libraryEventType, com.learnkafka.model.Book book) {
        return com.learnkafka.model.LibraryEvent.newBuilder()
                .setLibraryEventId(libraryEventId)
                .setLibraryEventType(libraryEventType)
                .setBook(book)
                .build();
    }

    private void verifyLibraryEventProcessed(com.learnkafka.model.LibraryEvent expectedLibraryEvent, com.learnkafka.model.Book expectedBook) throws IOException {
        verify(libraryEventsConsumerSpy, times(1))
                .onMessage(isA(ConsumerRecord.class), isA(MessageHeaders.class));
        verify(libraryEventsServiceSpy, times(1))
                .processLibraryEvent(isA(ConsumerRecord.class));

        libraryEventsRepository.findAll().forEach(actualLibraryEvent -> assertAll(
                () -> assertEquals(expectedLibraryEvent.getLibraryEventType().toString(), actualLibraryEvent.getLibraryEventType().toString()),
                () -> assertEquals(expectedBook.getId(), actualLibraryEvent.getBook().getId()),
                () -> assertEquals(expectedBook.getAuthor(), actualLibraryEvent.getBook().getAuthor()),
                () -> assertEquals(expectedBook.getName(), actualLibraryEvent.getBook().getName())
        ));
    }

    private void verifyUpdatedLibraryEvent(LibraryEvent libraryEvent, com.learnkafka.model.Book updatedBook) {
        LibraryEvent actualLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();

        assertAll(
                () -> assertEquals(LibraryEventType.UPDATE, actualLibraryEvent.getLibraryEventType()),
                () -> assertEquals(updatedBook.getId(), actualLibraryEvent.getBook().getId()),
                () -> assertEquals(updatedBook.getAuthor(), actualLibraryEvent.getBook().getAuthor()),
                () -> assertEquals(updatedBook.getName(), actualLibraryEvent.getBook().getName())
        );
    }
}
