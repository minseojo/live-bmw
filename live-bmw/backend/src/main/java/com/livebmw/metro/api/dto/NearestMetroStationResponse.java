package com.livebmw.metro.api.dto;

public record NearestMetroStationResponse(
        String stationId,
        String stationName,
        String lineId,
        String lineName,
        int distanceM // 소수점 버리고 m 단위 정수
) {}
