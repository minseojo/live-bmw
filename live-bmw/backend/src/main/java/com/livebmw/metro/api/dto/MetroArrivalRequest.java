package com.livebmw.metro.api.dto;

// 초기 버전은 환승 없이 동일 노선 기준으로 동작하도록 하고, lineId를 주면 모호성이 사라져서 구현이 쉬움.

import jakarta.validation.constraints.NotBlank;

/** 출발역~도착역 구간의 '방향'에 맞는 도착정보 조회 요청 */
public record MetroArrivalRequest(
        @NotBlank String originStation,
        @NotBlank String destinationStation,
        @NotBlank String lineId,
        Integer resultLimit // nullable 허용
) {
    public int limitOrDefault() {
        return (resultLimit == null || resultLimit <= 0) ? 5 : Math.min(resultLimit, 5);
    }
}
