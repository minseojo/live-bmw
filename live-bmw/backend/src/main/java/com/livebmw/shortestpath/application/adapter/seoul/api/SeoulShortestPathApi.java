package com.livebmw.shortestpath.application.adapter.seoul.api;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** HTTP 호출 전담 (테스트 용이성/책임 분리) */
@Slf4j
public class SeoulShortestPathApi {

    private final HttpClient http;

    public SeoulShortestPathApi(HttpClient http) {
        this.http = http;
    }

    public String get(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(7))
                .GET()
                .build();

        HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.body().isBlank()) {
            log.error("url:{}\nbody:{}", url, response.body());
            throw new IOException("url:" + url + "\nbody:" + response.body());
        }
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " calling getShtrmPath: " + response.body());
        }
        return response.body();
    }
}


