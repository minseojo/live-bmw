// com.livebmw.metro.domain.model.MetroArrival
package com.livebmw.metro.domain.model;

public record MetroArrival(
        String lineId,           // ex) "1002"
        String lineName,
        String directionLabel,   // ex) "내선"/"외선" 또는 "상행"/"하행"
        String trainLineSummary, // ex) "성수행 - 봉천방면"
        String trainNumber,      // ex) "1234"
        String message,          // arvlMsg2
        String messageSub,       // arvlMsg3
        int etaSeconds,          // 남은 초 (arvlMsg2 파싱 → 실패 시 barvlDt)
        String receivedAt        // ISO-8601 (서버 or recptnDt 기반)
) { }
