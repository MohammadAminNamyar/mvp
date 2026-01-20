package com.example.squarespool.config;

import com.example.squarespool.tpi.MockTpiClient;
import com.example.squarespool.tpi.TpiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TpiConfig {
    @Bean
    public TpiClient tpiClient(RestClient.Builder restClientBuilder, TpiProperties properties) {
        return new MockTpiClient(restClientBuilder, properties);
    }
}
