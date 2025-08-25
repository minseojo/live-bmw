package com.livebmw.shortestPath.application.adapter.seoul.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livebmw.shortestPath.api.dto.ShortestPathResponse;

import java.util.ArrayList;
import java.util.List;

public final class SeoulShortestPathParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SeoulShortestPathParser() {}
    public static ShortestPathResponse parseMinimal(String json) {
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

            List<ShortestPathResponse.Leg> legs = new ArrayList<>();
            JsonNode paths = body.path("paths");
            for (int i = 0; i < paths.size(); i++) {
                JsonNode p = paths.get(i);
                String fromName = optText(p, "dptreStn.stnNm");
                String fromLine = optText(p, "dptreStn.lineNm");
                String toName   = optText(p, "arvlStn.stnNm");
                String toLine   = optText(p, "arvlStn.lineNm");
                String dir      = optText(p, "upbdnbSe");  // 옵션: 실시간(상/하행·내/외선) 필터용

                legs.add(new ShortestPathResponse.Leg(
                        fromName, fromLine, toName, toLine, dir
                ));
            }

            return new ShortestPathResponse(searchType, List.copyOf(legs));

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse minimal ShortestPath JSON", e);
        }
    }

    private static JsonNode node(JsonNode root, String path) {
        JsonNode n = root;
        for (String k : path.split("\\.")) n = n.path(k);
        if (n.isMissingNode() || n.isNull()) throw new IllegalArgumentException("Missing node: " + path);
        return n;
    }

    private static String optText(JsonNode root, String path) {
        JsonNode n = root;
        for (String k : path.split("\\.")) n = n.path(k);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    private static int optInt(JsonNode root, String path) {
        String s = optText(root, path);
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException ignore) {
            // API가 숫자를 정수형으로 주기도 함
            JsonNode n = root;
            for (String k : path.split("\\.")) n = n.path(k);
            return n.isInt() ? n.asInt() : 0;
        }
    }
}
