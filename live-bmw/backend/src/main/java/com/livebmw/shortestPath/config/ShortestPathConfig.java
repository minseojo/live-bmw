package com.livebmw.shortestPath.config;

import com.livebmw.shortestPath.application.adapter.seoul.api.SeoulShortestPathApi;
import com.livebmw.shortestPath.application.adapter.seoul.api.SeoulShortestPathClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class ShortestPathConfig {

    @Bean
    public HttpClient shortestPathHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    @Bean
    public SeoulShortestPathApi seoulShortestPathApi(HttpClient shortestPathHttpClient) {
        return new SeoulShortestPathApi(shortestPathHttpClient);
    }

    @Bean
    public SeoulShortestPathClient seoulShortestPathClient(@Value("${seoul.shortest.path.api.key}") String serviceKey,
            SeoulShortestPathApi api) {
        return new SeoulShortestPathClient(serviceKey, "http://apis.data.go.kr/B553766/path", api);
    }
}


