package com.livebmw.shortestpath.domain;

import java.util.List;

public record ShortestPathPlan(
        String searchType,     // body.searchType (예: duration)
        List<Leg> legs         // 구간 리스트
) {
    /** 구간: 출발역/도착역의 역명과 호선명만 */
    public record Leg(
            String fromName,   // dptreStn.stnNm
            String fromLine,   // dptreStn.lineNm
            String toName,     // arvlStn.stnNm
            String toLine,     // arvlStn.lineNm
            String direction   // upbdnbSe: "내선"/"외선"/"상행"/"하행" (실시간 필터에 유용)
    ) {}
}
