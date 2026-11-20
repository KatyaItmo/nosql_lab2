package com.fitness.nosql_lab1.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CouchDbConfig {

    @Bean
    public RestClient couchDbRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:5984")
                .defaultHeader("Authorization", "Basic YWRtaW46cGFzc3dvcmQ=")
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
