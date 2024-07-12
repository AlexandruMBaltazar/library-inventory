package com.learnkafka.consumer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.model.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

    private final ConfigProperties configProperties;

    @KafkaListener(topics = "#{configProperties.libraryTopic}")
    public void onMessage(ConsumerRecord<Long, LibraryEvent> libraryEvent, @Headers MessageHeaders headers) {
        log.info("### -> Header acquired: {}", headers);
        log.info("### -> Event acquired: {}", libraryEvent);
    }
}
