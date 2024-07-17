package com.learnkafka.consumer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.model.LibraryEvent;
import com.learnkafka.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

    private final ConfigProperties configProperties;
    private final LibraryEventsService libraryEventsService;

    @KafkaListener(topics = "#{configProperties.libraryTopic}")
    public void onMessage(ConsumerRecord<Long, LibraryEvent> consumerRecord, @Headers MessageHeaders headers) throws IOException {
        log.info("### -> Header acquired: {}", headers);
        log.info("### -> Consumer record: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
