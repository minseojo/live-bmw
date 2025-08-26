package com.livebmw.metroStation.config;

import com.livebmw.common.util.GeometryFactory4326;
import com.livebmw.metroStation.domain.MetroStation;
import com.livebmw.metroStation.domain.MetroStationRepository;
import com.livebmw.metroStation.domain.model.MetroLine;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetroStationImporter {

    @Value("${livebmw.metro.station-import.csv}")
    Resource csvResource;

    private final MetroStationRepository stationRepo;
    private final GeometryFactory4326 geometryFactory4326;
    private final EntityManager entityManager;

    @Transactional
    public void importCsv() throws Exception {
        long existing = stationRepo.count();
        if (existing > 0) {
            log.info("[metro] skip import: metro_station has {} rows", existing);
            return;
        }
        if (csvResource == null || !csvResource.exists()) {
            throw new IllegalStateException("CSV resource not found: livebmw.metro.station-import.csv");
        }
        log.info("[metro] importing stations from {}", csvResource);
        try (Reader reader = new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {

            // 헤더 키
            final String COL_ID = "외구간_역_수";
            final String COL_NM = "역한글명칭";
            final String COL_LN = "호선명칭";
            final String COL_LON = "환승역X좌표";
            final String COL_LAT = "환승역Y좌표";
            // 예시: 4128,삼성중앙,9호선(연장),127.053282,37.513011

            int count = 0, bad = 0, ok = 0;
            for (var row : parser) {
                var stationId = safeGet(row, COL_ID).trim();
                var stationName = safeGet(row, COL_NM).trim();
                String lineName = safeGet(row, COL_LN).trim();
                log.info("[metro] importing station {}", lineName);
                var lon = Double.parseDouble(safeGet(row, COL_LON).trim());
                var lat = Double.parseDouble(safeGet(row, COL_LAT).trim());
                if (lon < 120 || lon > 135 || lat < 30 || lat > 45) {
                    bad++;
                    continue;
                }

                MetroLine metroLine = MetroLine.fromAny(lineName);
                if (metroLine == null) {
                    log.error("Metro line {} not found", lineName);
                    return;
                }
                int lineId = metroLine.getCode();
                lineName = metroLine.getDisplayName();

                var station = new MetroStation(stationId, stationName, lineId, lineName, geometryFactory4326.point(lon, lat));
                entityManager.merge(station);
                if (++count % 500 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
                ok++;

            }
            entityManager.flush();
            entityManager.clear();
            log.info("[metro] JPA import done. ok={}, bad={}", ok, bad);
        } catch (Exception e) {
            log.error("[metro] import failed", e);
        }
    }

    private static String safeGet(CSVRecord record, String key) {
        if (record.isMapped(key)) {
            return record.get(key);
        }
        String bomKey = "\uFEFF" + key;
        if (record.isMapped(bomKey)) {
            return record.get(bomKey);
        }

        Map<String, String> map = record.toMap();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = stripBom(e.getKey());
            if (key.equals(k)) return e.getValue();
        }
        throw new IllegalArgumentException("Mapping for " + key + " not found, available=" + map.keySet());
    }

    private static String stripBom(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }
}
