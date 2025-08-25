package com.livebmw.metro.application.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.livebmw.metro.api.dto.MetroArrivalResponse;
import com.livebmw.metro.application.adapter.seoul.dto.MetroArrivalXml;
import com.livebmw.metro.domain.model.MetroArrival;
import com.livebmw.metro.domain.model.MetroLine;
import com.livebmw.util.ArvlMsgParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MetroArrivalService {

    private final String apiKey;
    private final WebClient webClient;

    private final XmlMapper xmlMapper = new XmlMapper();

    // 시간/포맷 상수
    private final Clock clock;
    private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIMESTAMP_FMT_KST = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 메시지 파싱용 패턴
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)분");
    private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)초");


    @Autowired
    public MetroArrivalService(
            @Qualifier("seoulMetroWebClient") WebClient webClient,
            @Value("${seoul.live.metro.arrival.api.key}") String apiKey,
            org.springframework.beans.factory.ObjectProvider<Clock> clockProvider
    ) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.clock = clockProvider.getIfAvailable(() -> Clock.system(ZONE_KST));
    }

    /** 서울시 실시간 도착 정보 조회 (역명 기준) */
    public List<MetroArrival> fetchArrivals(String stationName) {
        final String encodedStation = UriUtils.encodePathSegment(stationName, StandardCharsets.UTF_8);

        final String xmlPayload = webClient.get()
                .uri("/{key}/xml/realtimeStationArrival/0/5/{station}", apiKey, stationName)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("encodedStation: {}", encodedStation);


        log.info(xmlPayload);

        if (xmlPayload == null || xmlPayload.isBlank()) {
            return List.of();
        }

        try {
            final MetroArrivalXml root = xmlMapper.readValue(xmlPayload, MetroArrivalXml.class);
            if (root.rows == null || root.rows.isEmpty()) return List.of();

            return root.rows.stream()
                    .map(row -> {
                        // 1. arvlMsg2 파싱 (초 단위)
                        Integer parsed = ArvlMsgParser.parseSeconds(row.arvlMsg2);
                        if (parsed == null) throw new RuntimeException("API 예외");
                        int eta = parsed;

                        // 2. 10초 보정 (최소 0초 이상)
                        eta = Math.max(0, eta - 10);

                        final String lineName = mapMetroLineName(row.subwayId);
                        return new MetroArrival(
                                row.subwayId,
                                lineName,
                                row.updnLine,
                                row.trainLineNm,
                                row.btrainNo,
                                row.arvlMsg2,
                                row.arvlMsg3,
                                eta,
                                row.recptnDt
                        );
                    })
                    .sorted(Comparator.comparingInt(MetroArrival::etaSeconds))
                    .limit(5)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Seoul subway XML", e);
        }
    }

    /** ETA(초) 계산: barvlDt(초) + recptnDt 보정, 메시지(진입/도착/출발/곧) 처리 */
    private int computeEtaSeconds(MetroArrivalXml.Row row, Instant now) {
        // 1) 서버가 내려준 남은 초(barvlDt)가 있으면 recptnDt 지연을 보정
        if (row.barvlDt != null) {
            long lagSec = 0;
            try {
                // recptnDt는 KST 기준 포맷
                final LocalDateTime receivedAtLocal = LocalDateTime.parse(row.recptnDt, TIMESTAMP_FMT_KST);
                final Instant receivedAt = receivedAtLocal.atZone(ZONE_KST).toInstant();
                lagSec = Duration.between(receivedAt, now).getSeconds();
            } catch (Exception ignored) {}

            long eta = (long) row.barvlDt - Math.max(0, lagSec);
            if (eta < 0) eta = 0;

            // "진입/도착/출발" 메시지는 즉시(0초)로 보정
            if (row.arvlMsg2 != null && row.arvlMsg2.matches(".*(진입|도착|출발).*")) {
                eta = 0;
            }
            return (int) eta;
        }

        // 2) barvlDt가 없으면 한국어 문구에서 분/초 파싱
        return parseEtaFromMessage(row.arvlMsg2);
    }

    /** "6분 후", "2분 20초 후", "곧 도착" 등 간단 파서 */
    private int parseEtaFromMessage(String message) {
        if (message == null || message.isBlank()) return 0;
        if (message.contains("곧")) return 0;

        int minutes = 0, seconds = 0;
        var m1 = MINUTES_PATTERN.matcher(message);
        var m2 = SECONDS_PATTERN.matcher(message);
        if (m1.find()) minutes = Integer.parseInt(m1.group(1));
        if (m2.find()) seconds = Integer.parseInt(m2.group(1));
        return minutes * 60 + seconds;
    }

    private String mapMetroLineName(String metroId) {
        return MetroLine.toDisplayName(metroId);
    }
}
