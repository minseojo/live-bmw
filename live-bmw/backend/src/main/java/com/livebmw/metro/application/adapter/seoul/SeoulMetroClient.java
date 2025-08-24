package com.livebmw.metro.application.adapter.seoul;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SeoulMetroClient {

    @Bean
    public WebClient seoulMetroWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://swopenAPI.seoul.go.kr/api/subway")
                .build();
    }
}
