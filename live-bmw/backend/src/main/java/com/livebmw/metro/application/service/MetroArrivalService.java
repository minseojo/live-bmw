package com.livebmw.metro.application.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.livebmw.common.time.DateTimeUtil;
import com.livebmw.metro.application.adapter.seoul.dto.MetroArrivalXml;
import com.livebmw.metro.domain.model.MetroArrival;
import com.livebmw.metroStation.domain.model.MetroLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class MetroArrivalService {

    private final String apiKey;
    private final WebClient webClient;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Autowired
    public MetroArrivalService(
            @Qualifier("seoulMetroWebClient") WebClient webClient,
            @Value("${seoul.live.metro.arrival.api.key}") String apiKey) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    /** 서울시 실시간 도착 정보 조회 (역명 기준) */
    public List<MetroArrival> fetchArrivals(String stationName) {
        final String xmlPayload = webClient.get()
                .uri("/{key}/xml/realtimeStationArrival/0/10/{station}", apiKey, stationName)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (xmlPayload == null || xmlPayload.isBlank()) {
            return List.of();
        }

        try {
            final MetroArrivalXml root = xmlMapper.readValue(xmlPayload, MetroArrivalXml.class);
            if (root.rows == null || root.rows.isEmpty()) return List.of();

            return root.rows.stream()
                    .map(row -> {
                        final String lineName = mapMetroLineName(row.subwayId);
                        return new MetroArrival(
                                row.subwayId,
                                lineName,
                                row.updnLine,
                                row.trainLineNm,
                                row.statnNm,
                                row.barvlDt,
                                DateTimeUtil.parseKst(row.recptnDt)
                        );
                    })
                    .sorted(Comparator.comparingInt(MetroArrival::etaSeconds))
                    .limit(5)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Seoul subway XML", e);
        }
    }

    private String mapMetroLineName(String metroId) {
        return MetroLine.toDisplayName(metroId);
    }
}
