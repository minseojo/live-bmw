package com.livebmw.shortestpath.application.adapter.seoul.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livebmw.shortestpath.domain.ShortestPathPlan;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class SeoulShortestPathParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SeoulShortestPathParser() {}

    public static ShortestPathPlan parseMinimal(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);

            // 헤더 체크
            String code = optText(root, "header.resultCode");
            if (!"00".equals(code)) {
                String msg = optText(root, "header.resultMsg");
                throw new IllegalStateException("ShortestPath API error: " + code + " - " + msg);
            }

            JsonNode body = node(root, "body");
            String searchType = optText(body, "searchType");

            List<ShortestPathPlan.Leg> legs = new ArrayList<>();
            JsonNode paths = body.path("paths");

            if (paths.isArray()) {
                for (int i = 0; i < paths.size(); i++) {
                    JsonNode p = paths.get(i);
                    String fromName = safe(optText(p, "dptreStn.stnNm"));
                    String fromLine = safe(optText(p, "dptreStn.lineNm"));
                    String toName   = safe(optText(p, "arvlStn.stnNm"));
                    String toLine   = safe(optText(p, "arvlStn.lineNm"));
                    String dir      = safe(optText(p, "upbdnbSe")); // 상/하행, 내/외선 정보

                    // 최소한 출발역/도착역 이름이 있어야 유효한 leg 로 판단
                    if (fromName != null && toName != null) {
                        legs.add(new ShortestPathPlan.Leg(fromName, fromLine, toName, toLine, dir));
                    } else {
                        log.warn("Skipping invalid leg: {}", p.toPrettyString());
                    }
                }
            } else {
                log.warn("paths node is missing or not an array");
            }

            log.info("Parsed legs: {}", legs);

            return new ShortestPathPlan(searchType != null ? searchType : "UNKNOWN", List.copyOf(legs));

        } catch (Exception e) {
            log.error("Failed to parse minimal ShortestPath JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse minimal ShortestPath JSON", e);
        }
    }

    private static JsonNode node(JsonNode root, String path) {
        JsonNode n = root;
        for (String k : path.split("\\.")) {
            if (n == null || n.isMissingNode()) {
                throw new IllegalArgumentException("Missing node: " + path);
            }
            n = n.path(k);
        }
        return n;
    }

    private static String optText(JsonNode root, String path) {
        if (root == null) return null;
        JsonNode n = root;
        for (String k : path.split("\\.")) {
            if (n == null || n.isMissingNode() || n.isNull()) {
                return null;
            }
            n = n.path(k);
        }
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
