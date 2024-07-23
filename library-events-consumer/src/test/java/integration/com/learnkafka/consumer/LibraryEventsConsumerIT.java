package com.learnkafka.consumer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.jpa.LibraryEventsRepository;
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
@EmbeddedKafka(topics = {"avro-library-tp"}, partitions = 3)
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

    // This endpoint registry has a hold of all listener containers
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

    @BeforeEach
    void setUp() {
        /*
            Make sure that the container that we have "LibraryEventsConsumer" is going to wait until all partitions
            are assigned to it
         */
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void shouldPublishNewLibraryEvent() throws ExecutionException, InterruptedException, IOException {
        // Given
        com.learnkafka.model.Book expectedBook = com.learnkafka.model.Book.newBuilder()
                .setId(123L)
                .setAuthor("Test Author")
                .setName("Test Name")
                .build();

        com.learnkafka.model.LibraryEvent expectedLibraryEvent = com.learnkafka.model.LibraryEvent.newBuilder()
                .setLibraryEventId(null)
                .setLibraryEventType(com.learnkafka.model.LibraryEventType.NEW)
                .setBook(expectedBook)
                .build();

        // When
        kafkaTemplate.send(configProperties.getLibraryTopic(), expectedLibraryEvent).get();

        // Then

        /*
            Consumer runs in a total different thread than the actual application thread
            It might take a while for the consumer to read the record and process it
         */
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
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
        });
    }
}
