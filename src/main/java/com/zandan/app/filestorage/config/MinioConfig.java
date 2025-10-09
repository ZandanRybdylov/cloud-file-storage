package com.zandan.app.filestorage.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(@Value("${minio.url}") String minioUrl,
                                   @Value("${minio.accessKey}") String minioAccessKey,
                                   @Value("${minio.secretKey}") String minioSecretKey) {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}
