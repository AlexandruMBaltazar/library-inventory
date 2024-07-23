package com.learnkafka.api.controller;

import com.learnkafka.api.model.Book;
import com.learnkafka.api.model.LibraryEvent;
import com.learnkafka.api.model.LibraryEventType;
import com.learnkafka.config.ConfigProperties;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"avro-library-tp"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://localhost:8081"
})
public class LibraryEventsControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Long, Object> consumer;

    @BeforeEach
    void setUp() {
        consumer = createKafkaConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    private Consumer<Long, Object> createKafkaConsumer() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        configs.put("schema.registry.url", "mock://localhost:8081");
        configs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return new DefaultKafkaConsumerFactory<>(configs, new LongDeserializer(), new KafkaAvroDeserializer()).createConsumer();
    }

    @Test
    void postLibraryEvent() {
s

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);
        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEvent.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ConsumerRecord<Long, Object> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, configProperties.getLibraryTopic());
        com.learnkafka.model.LibraryEvent consumedLibraryEvent = (com.learnkafka.model.LibraryEvent) consumerRecord.value();

        assertEquals(expectedLibraryEvent, consumedLibraryEvent);
    }
}


