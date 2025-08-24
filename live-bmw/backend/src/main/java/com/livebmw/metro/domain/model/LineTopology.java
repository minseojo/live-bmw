package com.livebmw.metro.domain.model;

import java.util.List;

/** 노선별 역 순서/원형 여부를 제공하는 도메인 서비스(포트) */
public interface LineTopology {
    /** 라인에 속한 역의 '정방향' 순서 리스트. (예: 상행 기준 or 내부 규약 기준) */
    List<String> stationsOf(String lineId);

    /** 원형 노선(내/외선) 여부 */
    boolean isCircular(String lineId);
}
