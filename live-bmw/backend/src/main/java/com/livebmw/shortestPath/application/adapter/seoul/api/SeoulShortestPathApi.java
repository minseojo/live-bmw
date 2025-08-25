package com.livebmw.shortestPath.application.adapter.seoul.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** HTTP 호출 전담 (테스트 용이성/책임 분리) */
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

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("HTTP " + res.statusCode() + " calling getShtrmPath: " + res.body());
        }
        return res.body();
    }
}


