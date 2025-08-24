package com.livebmw.metro.api.dto;

import java.util.List;

public record RouteArrivalsResponse(
        String lineId,
        String lineName,
        String originStation,
        String destinationStation,
        String direction,     // "상행"/"하행"/"내선"/"외선"
        String nextStation,   // 출발역 다음 역(해당 방향)
        List<MetroArrivalResponse> arrivals
) {}
