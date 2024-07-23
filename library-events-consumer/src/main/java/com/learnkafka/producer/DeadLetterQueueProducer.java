package com.learnkafka.producer;

import com.learnkafka.config.ConfigProperties;
import com.learnkafka.model.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueProducer {
    private final KafkaTemplate<Long, LibraryEvent> kafkaTemplate;
    private final ConfigProperties configProperties;

    public void send(ConsumerRecord<Long, LibraryEvent> consumerRecord) {
        kafkaTemplate.send(configProperties.getLibraryDeadLetterTopic(), consumerRecord.key(), consumerRecord.value());
        log.info("Sent to DLQ: {}", consumerRecord);
    }
}
