package com.yinft.minio.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    private static String url = "http://10.0.20.61:9000";

    private static String accessKey = "admin";

    private static String secretKey = "admin123";

    @Bean
    MinioClient minioClient() {
        return MinioClient.builder().endpoint(url).credentials(accessKey,secretKey).build();
    }
}