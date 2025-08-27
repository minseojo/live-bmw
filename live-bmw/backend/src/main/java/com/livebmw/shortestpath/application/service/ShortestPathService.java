package com.livebmw.shortestpath.application.service;

import com.livebmw.shortestpath.application.adapter.seoul.api.SeoulShortestPathClient;
import com.livebmw.shortestpath.application.adapter.seoul.api.SeoulShortestPathClient.SearchType;
import com.livebmw.shortestpath.application.adapter.seoul.dto.SeoulShortestPathParser;
import com.livebmw.shortestpath.domain.ShortestPathPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortestPathService {

    private final SeoulShortestPathClient client;

    public ShortestPathPlan findPlan(
            String departureStationName,  // 출발역
            String arrivalStationName,    // 도착역
            SearchType searchType) {      // 검색유형 duration(최소시간, default), distance(최단거리), transfer(최소환승)
        try {
            String json = client.getShortestPath(departureStationName, arrivalStationName, searchType);
            return SeoulShortestPathParser.parseMinimal(json);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch shortest path", e);
        }
    }

    public ShortestPathPlan findPlan(
            String departureStationName,
            String arrivalStationName,
            SearchType searchType,
            LocalDateTime when) {
        try {
            String json = client.getShortestPath(departureStationName, arrivalStationName, searchType, when);
            return SeoulShortestPathParser.parseMinimal(json);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
