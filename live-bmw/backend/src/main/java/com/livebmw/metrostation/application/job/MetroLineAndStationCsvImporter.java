package com.livebmw.metrostation.application.job;

import com.livebmw.metrostation.domain.entity.MetroLine;
import com.livebmw.metrostation.domain.entity.MetroLineAlias;
import com.livebmw.metrostation.domain.entity.MetroStation;
import com.livebmw.metrostation.domain.repository.MetroLineAliasRepository;
import com.livebmw.metrostation.domain.repository.MetroLineRepository;
import com.livebmw.metrostation.domain.repository.MetroStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetroLineAndStationCsvImporter {

    private final MetroLineRepository metroLineRepository;
    private final MetroLineAliasRepository metroLineAliasRepository;
    private final MetroStationRepository metroStationRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional
    public void importFrom() throws Exception {
        // 1) 리소스: classpath 우선, 없으면 /mnt/data 폴백
        Resource resource = new ClassPathResource("static/merged_realtime_with_station_master_geo.csv");
        InputStream is;
        if (resource.exists()) {
            is = resource.getInputStream();
            log.info("Using classpath resource: static/merged_realtime_with_station_master_geo.csv");
        } else {
            File fallback = new File("/mnt/data/merged_realtime_with_station_master_geo.csv");
            if (!fallback.exists()) {
                throw new IllegalStateException("CSV를 찾을 수 없습니다. classpath 또는 /mnt/data 경로를 확인하세요.");
            }
            is = new FileInputStream(fallback);
            log.info("Using filesystem resource: {}", fallback.getAbsolutePath());
        }

        Map<Integer, LineBucket> lineBuckets = new LinkedHashMap<>();
        int insertedStations = 0, updatedStations = 0, skippedNoCoord = 0;

        // 2) CSV 파싱
        try (InputStream in = is;
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setIgnoreSurroundingSpaces(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            Map<String, String> header = buildNormalizedHeaderMap(parser.getHeaderMap().keySet());

            for (CSVRecord r : parser) {
                // ----- Line bucket -----
                parseLineRecord(r, header).ifPresent(p -> {
                    LineBucket b = lineBuckets.computeIfAbsent(p.code, k -> new LineBucket());
                    b.display = pickBetterDisplay(b.display, p.display);
                    p.aliases.forEach(a -> addAlias(b.aliases, a));
                });

                // ----- Station upsert -----
                String stationId = firstNonBlank(
                        val(r, header, "STATN_ID"),
                        val(r, header, "전철역코드")
                );
                if (stationId == null || stationId.isBlank()) continue;

                String name = firstNonBlank(
                        val(r, header, "station_std"),
                        val(r, header, "전철역명"),
                        val(r, header, "STATN_NM")
                );
                if (name == null || name.isBlank()) continue;

                String subId = val(r, header, "SUBWAY_ID");
                if (subId == null || !subId.chars().allMatch(Character::isDigit)) continue;
                Integer lineCode = Integer.parseInt(subId);

                String lineName = firstNonBlank(val(r, header, "호선이름"), val(r, header, "line_std"));

                // 좌표 읽기
                String latStr = val(r, header, "lat");
                String lonStr = val(r, header, "lon");
                if (latStr.isBlank() || lonStr.isBlank()) {
                    skippedNoCoord++;
                    continue;
                }
                double lat, lon;
                try {
                    lat = Double.parseDouble(latStr);
                    lon = Double.parseDouble(lonStr);
                } catch (NumberFormatException e) {
                    skippedNoCoord++;
                    continue;
                }

                Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
                point.setSRID(4326);

                // upsert
                MetroStation station = metroStationRepository.findById(stationId).orElse(null);
                if (station == null) {
                    station = new MetroStation(
                            stationId,
                            name,
                            lineCode,
                            lineName != null ? lineName : String.valueOf(lineCode),
                            point
                    );
                    metroStationRepository.save(station);
                    insertedStations++;
                } else {
                    boolean dirty = false;
                    if (!Objects.equals(station.getName(), name)) { setField(station, "name", name); dirty = true; }
                    if (station.getLineId() != lineCode) { setField(station, "lineId", lineCode); dirty = true; }
                    if (!Objects.equals(station.getLineName(), lineName)) { setField(station, "lineName", lineName); dirty = true; }
                    if (station.getGeom() == null || !station.getGeom().equalsExact(point)) { setField(station, "geom", point); dirty = true; }
                    if (dirty) {
                        metroStationRepository.save(station);
                        updatedStations++;
                    }
                }
            }
        }

        // 3) Line/Alias upsert
        int upsertLine = 0, upsertAlias = 0;
        for (Map.Entry<Integer, LineBucket> e : lineBuckets.entrySet()) {
            Integer code = e.getKey();
            LineBucket b = e.getValue();

            MetroLine line = metroLineRepository.findById(code).orElseGet(() -> new MetroLine(code, b.display));
            line.setDisplayName(b.display);
            metroLineRepository.save(line);
            upsertLine++;

            for (String raw : b.aliases) {
                String normalized = normalize(raw);
                if (normalized == null || normalized.isBlank()) continue;
                if (metroLineAliasRepository.findByNormalizedName(normalized).isPresent()) continue;

                MetroLineAlias alias = MetroLineAlias.builder()
                        .rawName(raw)
                        .normalizedName(normalized)
                        .line(line)
                        .build();
                metroLineAliasRepository.save(alias);
                upsertAlias++;
            }
        }

        log.info("Lines upserted: {}, Aliases upserted: {}", upsertLine, upsertAlias);
        log.info("Stations inserted: {}, updated: {}, skipped (no/invalid coord): {}", insertedStations, updatedStations, skippedNoCoord);
    }

    /* --------------------- CSV helpers --------------------- */

    private Optional<LineParsed> parseLineRecord(CSVRecord r, Map<String,String> header) {
        try {
            String subId  = val(r, header, "SUBWAY_ID");
            String lineStd = val(r, header, "line_std");
            String lineKo  = val(r, header, "호선이름");
            String lineTag = val(r, header, "호선");
            String lineEn  = val(r, header, "전철명명(영문)");

            if (subId == null || !subId.chars().allMatch(Character::isDigit)) return Optional.empty();
            int code = Integer.parseInt(subId);
            String display = firstNonBlank(lineKo, lineStd, String.valueOf(code));

            List<String> aliases = new ArrayList<>();
            Collections.addAll(aliases, lineKo, lineStd, lineTag, lineEn);
            return Optional.of(new LineParsed(code, display, aliases));
        } catch (Exception e) {
            log.warn("CSV line-parse skip: row={} err={}", r.getRecordNumber(), e.toString());
            return Optional.empty();
        }
    }

    private static Map<String, String> buildNormalizedHeaderMap(Set<String> rawHeaders) {
        Map<String, String> map = new HashMap<>();
        for (String h : rawHeaders) {
            String key = normalize(h);
            if (key != null) key = key.toLowerCase(Locale.ROOT);
            map.put(key, h);
        }
        return map;
    }

    private static String val(CSVRecord r, Map<String,String> header, String wanted) {
        String norm = normalize(wanted);
        if (norm != null) norm = norm.toLowerCase(Locale.ROOT);
        String real = header.get(norm);
        if (real == null) return "";
        try {
            String v = r.get(real);
            return v == null ? "" : v.trim();
        } catch (Exception e) {
            return "";
        }
    }

    /* --------------------- common utils --------------------- */

    private static String firstNonBlank(String... s) {
        for (String v : s) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static void addAlias(Set<String> set, String s) {
        if (s == null || s.isBlank()) return;
        String s1 = s.replaceAll("^0+([0-9])호선$", "$1호선"); // "01호선" → "1호선"
        set.add(s);
        set.add(s1);
    }

    private static String pickBetterDisplay(String a, String b) {
        if (a == null || a.isBlank()) return b;
        if (b == null || b.isBlank()) return a;
        int scoreA = score(a), scoreB = score(b);
        return (scoreB > scoreA) ? b : a;
    }

    private static int score(String s) {
        int len = s.length();
        boolean hasKo = s.chars().anyMatch(ch -> Character.UnicodeScript.of(ch) == Character.UnicodeScript.HANGUL);
        boolean hasDigit = s.chars().anyMatch(Character::isDigit);
        return len + (hasKo ? 5 : 0) + (hasDigit ? 1 : 0);
    }

    public static String normalize(String s) {
        if (s == null) return null;
        return Normalizer.normalize(s, Normalizer.Form.NFKC)
                .replace("\uFEFF","")
                .replaceAll("\\s+","")
                .trim();
    }

    private record LineParsed(int code, String display, List<String> aliases) {}
    private static class LineBucket {
        String display;
        Set<String> aliases = new LinkedHashSet<>();
    }

    /** 롬복 @Setter가 없다면 반영용(필드 접근). 가능하면 명시적 setter를 만드는 걸 권장 */
    private static void setField(MetroStation s, String field, Object value) {
        try {
            var f = MetroStation.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(s, value);
        } catch (Exception ignore) {}
    }
}
