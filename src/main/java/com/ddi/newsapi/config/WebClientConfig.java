package com.ddi.newsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // WebClient.Builder를 사용하여 WebClient 인스턴스를 생성합니다.
        // 필요에 따라 .baseUrl(), .defaultHeader() 등을 설정할 수 있습니다.
        return WebClient.builder().build();
    }
}