package com.zandan.app.filestorage.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic fileOperationEventTopic() {
        return TopicBuilder.name("file-operated-topic")
                .partitions(1)
                .build();
    }
}
