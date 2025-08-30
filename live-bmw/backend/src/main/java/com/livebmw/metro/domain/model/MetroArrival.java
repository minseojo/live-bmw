package com.livebmw.metro.domain.model;

import java.time.LocalDateTime;

import static java.time.Duration.between;

public record MetroArrival(
        Integer lineId,           // ex) 1002
        String direction,        // ex) "내선"/"외선" 또는 "상행"/"하행"
        String trainLineName,    // ex) "성수행 - 봉천방면"
        String trainNumber,      // ex) "1234"
        int etaSeconds,          // API 가져왔을 당시 예상 도착시간
        LocalDateTime receivedAt // API 가져온 시간 recptnDt 기반
) {
    public int computeEtaSeconds() {
        // API 수신 시각 + 당시 ETA = 실제 도착 예정 시각
        LocalDateTime expectedArrivalTime = receivedAt.plusSeconds(etaSeconds);

        // 현재 시각과 도착 예정 시각의 차이 계산
        long diff = between(LocalDateTime.now(), expectedArrivalTime).getSeconds();

        // 0보다 작으면 이미 도착했다고 보고 최소 0 반환
        return (int) Math.max(0, diff);
    }

}
