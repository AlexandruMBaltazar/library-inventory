package com.learnkafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class AutoCreateConfig {

    private final ConfigProperties configProperties;

    @Bean
    public NewTopic libraryTopic() {
        return TopicBuilder.name(configProperties.getLibraryTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
