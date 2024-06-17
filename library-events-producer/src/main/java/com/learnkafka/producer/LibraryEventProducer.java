package com.learnkafka.producer;

import com.google.gson.Gson;
import com.learnkafka.config.ConfigProperties;
import com.learnkafka.model.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryEventProducer {

    private final ConfigProperties configProperties;
    private final KafkaTemplate<Long, LibraryEvent> kafkaTemplate;

    public String sendLibraryEvent(LibraryEvent libraryEvent) {
        CompletableFuture<SendResult<Long, LibraryEvent>> completableFuture = kafkaTemplate
                .send(createProducerRecord(libraryEvent.getLibraryEventId(), libraryEvent))
                .toCompletableFuture();

        String messageJson = new Gson().toJson(libraryEvent);
        log.info("#### -> Producing message -> {}", messageJson);

        completableFuture
                .thenAccept(result -> log.info("#### -> Message sent successfully -> {}", result))
                .exceptionally(ex -> {
            log.error("#### -> Failed to send message -> {}", ex.getMessage(), ex);
            return null;
        });

        return "success";
    }

    private ProducerRecord<Long, LibraryEvent> createProducerRecord(long key, LibraryEvent body) {
        return new ProducerRecord<>(configProperties.getLibraryTopic(), key, body);
    }
}
