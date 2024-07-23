package com.learnkafka.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ConfigProperties {
    @Value("${topic.library}")
    private String libraryTopic;

    @Value("${topic.dead-letter-topic}")
    private String libraryDeadLetterTopic;
}
