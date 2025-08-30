//package com.livebmw.metro.application.job;
//
//import com.livebmw.metro.domain.entity.MetroLine;
//import com.livebmw.metro.domain.entity.MetroStation;
//import com.livebmw.metro.domain.repository.MetroLineRepository;
//import com.livebmw.metro.domain.repository.MetroStationRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.Point;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.text.Normalizer;
//import java.util.*;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MetroLineAndStationCsvImporter {
//
//    private final MetroLineRepository metroLineRepository;
//    private final MetroStationRepository metroStationRepository;
//
//    private final GeometryFactory geometryFactory = new GeometryFactory();
//
//    @Transactional
//    public void importFrom() throws Exception {
//        // 1) CSV 파일 로드: classpath 우선, 없으면 /mnt/data 폴백
//        Resource resource = new ClassPathResource("static/merged_realtime_with_station_master_geo.csv");
//        InputStream is;
//        if (resource.exists()) {
//            is = resource.getInputStream();
//            log.info("Using classpath resource: static/merged_realtime_with_station_master_geo.csv");
//        } else {
//            File fallback = new File("/mnt/data/merged_realtime_with_station_master_geo.csv");
//            if (!fallback.exists()) {
//                throw new IllegalStateException("CSV를 찾을 수 없습니다. classpath 또는 /mnt/data 경로를 확인하세요.");
//            }
//            is = new FileInputStream(fallback);
//            log.info("Using filesystem resource: {}", fallback.getAbsolutePath());
//        }
//
//        // 2) 1차 파싱: 라인/역 정보 수집
//        Map<Integer, LineBucket> lineBuckets = new LinkedHashMap<>();
//        List<StationParsed> stationRows = new ArrayList<>();
//        int skippedNoCoord = 0;
//
//        try (InputStream in = is;
//             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
//             CSVParser parser = CSVFormat.DEFAULT.builder()
//                     .setHeader()
//                     .setSkipHeaderRecord(true)
//                     .setIgnoreEmptyLines(true)
//                     .setIgnoreSurroundingSpaces(true)
//                     .setTrim(true)
//                     .build()
//                     .parse(reader)) {
//
//            Map<String, String> header = buildNormalizedHeaderMap(parser.getHeaderMap().keySet());
//
//            for (CSVRecord r : parser) {
//                // ----- Line bucket -----
//                parseLineRecord(r, header).ifPresent(p -> {
//                    LineBucket b = lineBuckets.computeIfAbsent(p.code, k -> new LineBucket());
//                    b.lineName = pickPreferredName(b.lineName, p.lineName, p.code);
//                    p.aliases.forEach(a -> addAlias(b.aliases, a));
//                });
//
//                // ----- Station collect -----
//                String stationId = firstNonBlank(
//                        val(r, header, "STATN_ID"),
//                        val(r, header, "전철역코드")
//                );
//                if (stationId == null || stationId.isBlank()) continue;
//
//                String name = firstNonBlank(
//                        val(r, header, "station_std"),
//                        val(r, header, "전철역명"),
//                        val(r, header, "STATN_NM")
//                );
//                if (name == null || name.isBlank()) continue;
//
//                String subId = val(r, header, "SUBWAY_ID");
//                if (subId == null || !subId.chars().allMatch(Character::isDigit)) continue;
//                Integer lineCode = Integer.parseInt(subId);
//
//                // 좌표
//                String latStr = val(r, header, "lat");
//                String lonStr = val(r, header, "lon");
//                if (latStr.isBlank() || lonStr.isBlank()) { skippedNoCoord++; continue; }
//
//                double lat, lon;
//                try {
//                    lat = Double.parseDouble(latStr);
//                    lon = Double.parseDouble(lonStr);
//                } catch (NumberFormatException e) {
//                    skippedNoCoord++;
//                    continue;
//                }
//
//                Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
//                point.setSRID(4326);
//
//                stationRows.add(new StationParsed(stationId, name, lineCode, point));
//            }
//        }
//
//        // 3) Line/Alias upsert (선행)
//        int upsertLine = 0, upsertAlias = 0;
//        for (Map.Entry<Integer, LineBucket> e : lineBuckets.entrySet()) {
//            Integer code = e.getKey();
//            LineBucket b = e.getValue();
//
//            String finalLineName = (b.lineName == null || b.lineName.isBlank())
//                    ? String.valueOf(code) : b.lineName;
//
//            MetroLine line = metroLineRepository.findById(code)
//                    .orElseGet(() -> MetroLine.builder()
//                            .lineId(code)
//                            .lineName(finalLineName)
//                            .build());
//
//            if (!Objects.equals(line.getLineName(), finalLineName)) {
//                line.setLineName(finalLineName);
//            }
//            metroLineRepository.save(line);
//            upsertLine++;
//
//            for (String raw : b.aliases) {
//                String normalized = normalize(raw);
//                if (normalized == null || normalized.isBlank()) continue;
//
//                upsertAlias++;
//            }
//        }
//
//        // 4) Station upsert (MetroLine FK 연결)
//        int insertedStations = 0, updatedStations = 0;
//        for (StationParsed sp : stationRows) {
//            MetroLine line = metroLineRepository.findById(sp.lineCode).orElse(null);
//            if (line == null) {
//                log.warn("Line not found for station. stationId={}, lineCode={}", sp.stationId, sp.lineCode);
//                continue;
//            }
//
//            MetroStation station = metroStationRepository.findById(sp.stationId).orElse(null);
//            if (station == null) {
//                station = new MetroStation(
//                        sp.stationId,
//                        sp.name,
//                        line,
//                        sp.point
//                );
//                metroStationRepository.save(station);
//                insertedStations++;
//            } else {
//                boolean dirty = false;
//                if (!Objects.equals(station.getName(), sp.name)) { setField(station, "name", sp.name); dirty = true; }
////                if (station.getGeom() == null || !station.getGeom().equalsExact(sp.point)) { setField(station, "geom", sp.point); dirty = true; }
//                if (station.getLine() == null || !Objects.equals(station.getLine().getLineId(), line.getLineId())) {
//                    setField(station, "line", line);
//                    dirty = true;
//                }
//                if (dirty) {
//                    metroStationRepository.save(station);
//                    updatedStations++;
//                }
//            }
//        }
//
//        log.info("Lines upserted: {}, Aliases upserted: {}", upsertLine, upsertAlias);
//        log.info("Stations inserted: {}, updated: {}, skipped (no/invalid coord): {}", insertedStations, updatedStations, skippedNoCoord);
//    }
//
//    /* --------------------- CSV helpers --------------------- */
//
//    private Optional<LineParsed> parseLineRecord(CSVRecord r, Map<String,String> header) {
//        try {
//            String subId   = val(r, header, "SUBWAY_ID");
//            String lineStd = val(r, header, "line_std");     // 내부 표준 후보
//            String lineKo  = val(r, header, "호선이름");      // 한국어 표기
//            String lineTag = val(r, header, "호선");
//            String lineEn  = val(r, header, "전철명명(영문)");
//
//            if (subId == null || !subId.chars().allMatch(Character::isDigit)) return Optional.empty();
//            int code = Integer.parseInt(subId);
//
//            // lineName 후보: std > ko > code
//            String lineName = firstNonBlank(lineStd, lineKo, String.valueOf(code));
//
//            List<String> aliases = new ArrayList<>();
//            Collections.addAll(aliases, lineKo, lineStd, lineTag, lineEn);
//
//            return Optional.of(new LineParsed(code, lineName, aliases));
//        } catch (Exception e) {
//            log.warn("CSV line-parse skip: row={} err={}", r.getRecordNumber(), e.toString());
//            return Optional.empty();
//        }
//    }
//
//    private static Map<String, String> buildNormalizedHeaderMap(Set<String> rawHeaders) {
//        Map<String, String> map = new HashMap<>();
//        for (String h : rawHeaders) {
//            String key = normalize(h);
//            if (key != null) key = key.toLowerCase(Locale.ROOT);
//            map.put(key, h);
//        }
//        return map;
//    }
//
//    private static String val(CSVRecord r, Map<String,String> header, String wanted) {
//        String norm = normalize(wanted);
//        if (norm != null) norm = norm.toLowerCase(Locale.ROOT);
//        String real = header.get(norm);
//        if (real == null) return "";
//        try {
//            String v = r.get(real);
//            return v == null ? "" : v.trim();
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    /* --------------------- common utils --------------------- */
//
//    private static String firstNonBlank(String... s) {
//        for (String v : s) if (v != null && !v.isBlank()) return v;
//        return null;
//    }
//
//    private static void addAlias(Set<String> set, String s) {
//        if (s == null || s.isBlank()) return;
//        String s1 = s.replaceAll("^0+([0-9])호선$", "$1호선"); // "01호선" → "1호선"
//        set.add(s);
//        set.add(s1);
//    }
//
//    /** 기존값 a가 없으면 b, 둘 다 있으면 "더 나은" 이름 선택 (한글/숫자 포함 유리, 길이 긴 쪽 우선) */
//    private static String pickPreferredName(String a, String b, int fallbackCode) {
//        if (a == null || a.isBlank()) return (b == null || b.isBlank()) ? String.valueOf(fallbackCode) : b;
//        if (b == null || b.isBlank()) return a;
//        int sa = score(a), sb = score(b);
//        return (sb > sa) ? b : a;
//    }
//
//    private static int score(String s) {
//        int len = s.length();
//        boolean hasKo = s.chars().anyMatch(ch -> Character.UnicodeScript.of(ch) == Character.UnicodeScript.HANGUL);
//        boolean hasDigit = s.chars().anyMatch(Character::isDigit);
//        return len + (hasKo ? 5 : 0) + (hasDigit ? 1 : 0);
//    }
//
//    public static String normalize(String s) {
//        if (s == null) return null;
//        return Normalizer.normalize(s, Normalizer.Form.NFKC)
//                .replace("\uFEFF","")
//                .replaceAll("\\s+","")
//                .trim();
//    }
//
//    private record LineParsed(int code, String lineName, List<String> aliases) {}
//    private static class LineBucket {
//        String lineName;
//        Set<String> aliases = new LinkedHashSet<>();
//    }
//
//    private record StationParsed(String stationId, String name, Integer lineCode, Point point) {}
//
//    /** 롬복 @Setter가 없다면 반영용(필드 접근). 가능하면 명시적 setter를 만드는 걸 권장 */
//    private static void setField(MetroStation s, String field, Object value) {
//        try {
//            var f = MetroStation.class.getDeclaredField(field);
//            f.setAccessible(true);
//            f.set(s, value);
//        } catch (Exception ignore) {}
//    }
//}
