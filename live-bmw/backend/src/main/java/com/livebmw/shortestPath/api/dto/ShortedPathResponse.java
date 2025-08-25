package com.livebmw.shortestPath.api.dto;

import com.livebmw.metro.api.dto.MetroArrivalResponse;
import com.livebmw.shortestPath.domain.ShortestPathPlan;

import java.util.List;
import java.util.Map;

public record ShortedPathResponse(
        ShortestPathPlan plan,
        List<KeyStation> keyStations,
        Map<KeyStation, List<MetroArrivalResponse>> arrivalsByStation
) {}
