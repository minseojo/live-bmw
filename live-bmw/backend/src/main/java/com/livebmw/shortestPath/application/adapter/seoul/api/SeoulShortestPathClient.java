package com.livebmw.shortestPath.application.adapter.seoul.api;

import com.livebmw.common.time.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 서울교통공사_최단경로이동정보(getShtrmPath) 호출 전용 최소 클라이언트.
 * - 엔드포인트: http://apis.data.go.kr/B553766/path/getShtrmPath
 * - 필수 파라미터: serviceKey, dptreStnNm, arvlStnNm, searchDt
 * - 선택 파라미터: searchType(duration|distance|transfer), thrghStnNms, exclTrfstnNms, schInclYn(Y/N), dataType(JSON|XML)
 */

@RequiredArgsConstructor
public class SeoulShortestPathClient {

    public enum SearchType { duration, distance, transfer } // duration(최소시간, default), distance(최단거리), transfer(최소환승)
    private enum DataType { JSON }

    @Value("${seoul.shortest.path.api.key}")
    private final String serviceKey;
    private final String baseUrl;
    private final SeoulShortestPathApi seoulShortestPathApi;

    public String getShortestPath(String departureStationName, String arrivalStationName) throws IOException, InterruptedException {
        return getShortestPath(departureStationName, arrivalStationName, SearchType.duration, LocalDateTime.now(DateTimeUtil.KST));
    }

    public String getShortestPath(String departureStationName, String arrivalStationName, SearchType searchType) throws IOException, InterruptedException {
        return getShortestPath(departureStationName, arrivalStationName, searchType, LocalDateTime.now(DateTimeUtil.KST));
    }

    public String getShortestPath(String departureStationName, String arrivalStationName, LocalDateTime searchDateTime) throws IOException, InterruptedException {
        return getShortestPath(departureStationName, arrivalStationName, SearchType.duration, searchDateTime);
    }

    public String getShortestPath(String departureStationName, String arrivalStationName, SearchType searchType, LocalDateTime searchDateTime)
            throws IOException, InterruptedException
    {
        String url = baseUrl + "/getShtrmPath" +
                "?serviceKey=" + serviceKey +
                "&dataType=" + DataType.JSON +
                "&dptreStnNm=" + encode(departureStationName) +
                "&arvlStnNm=" + encode(arrivalStationName) +
                "&searchDt=" + encode(DateTimeUtil.formatKst(searchDateTime)) +
                "&searchType=" + searchType.name();
        System.out.println(url);
        return seoulShortestPathApi.get(url);
    }

    private static String encode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
