//package com.livebmw.metrostation.application.job;
//
//import com.livebmw.metrostation.domain.entity.MetroLine;
//import com.livebmw.metrostation.domain.entity.MetroLineAlias;
//import com.livebmw.metrostation.domain.repository.MetroLineAliasRepository;
//import com.livebmw.metrostation.domain.repository.MetroLineRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
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
//@RequiredArgsConstructor
//// unused
//public class MetroLineCsvImporter {
//
//    private final MetroLineRepository metroLineRepository;
//    private final MetroLineAliasRepository metroLineAliasRepository;
//
//    @Transactional
//    public void importFrom() throws Exception {
//        // 1) 리소스 확보: 클래스패스 우선, 없으면 /mnt/data 경로 fallback
//        Resource resource = new ClassPathResource("static/merged_realtime_with_station_master.csv");
//        InputStream is = null;
//        if (resource.exists()) {
//            is = resource.getInputStream();
//            log.info("Using classpath resource: static/merged_realtime_with_station_master.csv");
//        } else {
//            File fallback = new File("/mnt/data/merged_realtime_with_station_master.csv");
//            if (!fallback.exists()) {
//                throw new IllegalStateException(
//                        "CSV를 찾을 수 없습니다. classpath:static/merged_realtime_with_station_master.csv 또는 /mnt/data/merged_realtime_with_station_master.csv"
//                );
//            }
//            is = new FileInputStream(fallback);
//            log.info("Using filesystem resource: {}", fallback.getAbsolutePath());
//        }
//
//        Map<Integer, LineBucket> buckets = new LinkedHashMap<>();
//
//        // 2) CSV 파싱 (UTF-8-SIG 대응, 헤더 자동/스킵, 공백 트림)
//        try (InputStream in = is;
//             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
//             CSVParser parser = CSVFormat.DEFAULT
//                     .builder()
//                     .setHeader()                    // 첫 행을 헤더로
//                     .setSkipHeaderRecord(true)      // 데이터에서는 스킵
//                     .setIgnoreEmptyLines(true)
//                     .setIgnoreSurroundingSpaces(true)
//                     .setTrim(true)
//                     .build()
//                     .parse(reader)) {
//
//            // 2-1) 헤더 정규화 맵 구성 (BOM/공백/호환문자 제거)
//            Map<String, String> headerMap = buildNormalizedHeaderMap(parser.getHeaderMap().keySet());
//
//            for (CSVRecord r : parser) {
//                parseRecordSafe(r, headerMap).ifPresent(p -> {
//                    LineBucket b = buckets.computeIfAbsent(p.code, k -> new LineBucket());
//                    b.display = pickBetterDisplay(b.display, p.display);
//                    p.aliases.forEach(a -> addAlias(b.aliases, a));
//                });
//            }
//        }
//
//        // 3) DB upsert
//        int upsertLine = 0, upsertAlias = 0;
//        for (Map.Entry<Integer, LineBucket> e : buckets.entrySet()) {
//            Integer code = e.getKey();
//            LineBucket b = e.getValue();
//
//            MetroLine line = metroLineRepository.findById(code)
//                    .orElseGet(() -> new MetroLine(code, b.display));
//            line.setDisplayName(b.display);
//            metroLineRepository.save(line);
//            upsertLine++;
//
//            for (String raw : b.aliases) {
//                String normalized = normalize(raw);
//                if (normalized == null || normalized.isBlank()) continue;
//                if (metroLineAliasRepository.findByNormalizedName(normalized).isPresent()) continue;
//
//                MetroLineAlias alias = MetroLineAlias.builder()
//                        .rawName(raw)
//                        .normalizedName(normalized)
//                        .line(line)
//                        .build();
//                metroLineAliasRepository.save(alias);
//                upsertAlias++;
//            }
//        }
//
//        log.info("MetroLine upserted: {}, MetroLineAlias upserted: {}", upsertLine, upsertAlias);
//    }
//
//    /* ===================== 파싱 ===================== */
//
//    private Optional<Parsed> parseRecordSafe(CSVRecord r, Map<String, String> headerMap) {
//        try {
//            String subId  = val(r, headerMap, "SUBWAY_ID");
//            String lineStd = val(r, headerMap, "line_std");
//            String lineKo  = val(r, headerMap, "호선이름");
//            String lineTag = val(r, headerMap, "호선");          // 01호선 등
//            String lineEn  = val(r, headerMap, "전철명명(영문)");
//
//            if (subId == null || subId.isBlank() || !subId.chars().allMatch(Character::isDigit)) {
//                return Optional.empty();
//            }
//            int code = Integer.parseInt(subId);
//
//            String display = firstNonBlank(lineKo, lineStd, String.valueOf(code));
//
//            List<String> aliases = new ArrayList<>();
//            Collections.addAll(aliases, lineKo, lineStd, lineTag, lineEn);
//
//            return Optional.of(new Parsed(code, display, aliases));
//        } catch (Exception e) {
//            log.warn("CSV parse skip: row={} err={}", r.getRecordNumber(), e.toString());
//            return Optional.empty();
//        }
//    }
//
//    /** 헤더 정규화: NFKC → BOM 제거 → 공백 제거 → 소문자 */
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
//    /** 안전한 헤더 접근: 정규화된 키로 원본 헤더명 찾아서 값 읽기 */
//    private static String val(CSVRecord r, Map<String, String> headerMap, String wanted) {
//        String norm = normalize(wanted);
//        if (norm != null) norm = norm.toLowerCase(Locale.ROOT);
//        String real = headerMap.get(norm);
//        if (real == null) return "";
//        try {
//            String v = r.get(real);
//            return v == null ? "" : v.trim();
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//    /* ===================== 공통 유틸 ===================== */
//
//    private static String firstNonBlank(String... s) {
//        for (String v : s) if (v != null && !v.isBlank()) return v;
//        return null;
//    }
//
//    private static void addAlias(Set<String> set, String s) {
//        if (s == null || s.isBlank()) return;
//        String s1 = s.replaceAll("^0+([0-9])호선$", "$1호선"); // "01호선" -> "1호선"
//        set.add(s);
//        set.add(s1);
//    }
//
//    private static String pickBetterDisplay(String a, String b) {
//        if (a == null || a.isBlank()) return b;
//        if (b == null || b.isBlank()) return a;
//        int scoreA = score(a), scoreB = score(b);
//        return (scoreB > scoreA) ? b : a;
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
//    private record Parsed(int code, String display, List<String> aliases) {}
//    private static class LineBucket {
//        String display;
//        Set<String> aliases = new LinkedHashSet<>();
//    }
//}
