package com.learnkafka.consumer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.exception.RetryableMessagingException;
import com.learnkafka.model.LibraryEvent;
import com.learnkafka.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsConsumer {

    private final ConfigProperties configProperties;
    private final LibraryEventsService libraryEventsService;

    /*
        Things to keep in mind
       !!! Consumer runs in a total different thread than the actual application thread !!!
     */
    @KafkaListener(topics = "#{configProperties.libraryTopic}")
    @RetryableTopic(
            backoff = @Backoff(delay = 2000L, multiplier = 2),
            attempts = "2",
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.MULTIPLE_TOPICS,
            include = {RetryableMessagingException.class}
    )
    public void onMessage(ConsumerRecord<Long, LibraryEvent> consumerRecord, @Headers MessageHeaders headers) {
        log.info("### -> Header acquired: {}", headers);
        log.info("### -> Consumer record: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }

    @DltHandler
    public void dlt(ConsumerRecord<Long, LibraryEvent> consumerRecord, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Event from topic " + topic + " is dead lettered - event:" + consumerRecord);
    }
}
