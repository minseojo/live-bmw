package com.livebmw.metro.api.dto;

import com.livebmw.common.time.DateTimeUtil;
import com.livebmw.metro.domain.model.MetroArrival;

import java.util.List;

/**
 * API 응답용 DTO
 * - 도메인 MetroArrival -> 클라이언트 응답 뷰 모델 변환
 */
public record MetroArrivalResponse(
        String lineId,           // ex) "1002"
        String lineName,
        String direction,   // ex) "내선"/"외선" 또는 "상행"/"하행"
        String trainLineSummary, // ex) "성수행 - 봉천방면"
        String trainNumber,      // ex) "1234"
        int etaSeconds,          // 도착 예정시간
        String receivedAt        // api 요청했던 시간
) {
    /** 단건 변환 */
    public static MetroArrivalResponse from(MetroArrival arrival) {
        return new MetroArrivalResponse(
                arrival.lineId(),
                arrival.lineName(),
                arrival.direction(),
                arrival.trainLineName(),
                arrival.trainNumber(),
                arrival.computeEtaSeconds(),
                DateTimeUtil.formatKst(arrival.receivedAt())
        );
    }

    /** 리스트 변환 편의 메서드 */
    public static List<MetroArrivalResponse> from(List<MetroArrival> list) {
        return list.stream().map(MetroArrivalResponse::from).toList();
    }
}
