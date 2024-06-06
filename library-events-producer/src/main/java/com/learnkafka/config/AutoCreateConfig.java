package com.learnkafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AutoCreateConfig {

    @Value("${topic.library}")
    private String libraryTopic;

    @Bean
    public NewTopic libraryTopic() {
        return TopicBuilder.name(libraryTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
