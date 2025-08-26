package com.livebmw.metroStation.domain.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum MetroLine {
    // 서울 도시철도
    LINE_1(1001, "1호선"),
    LINE_2(1002, "2호선"),
    LINE_3(1003, "3호선"),
    LINE_4(1004, "4호선"),
    LINE_5(1005, "5호선"),
    LINE_6(1006, "6호선"),
    LINE_7(1007, "7호선"),
    LINE_8(1008, "8호선"),
    LINE_9(1009, "9호선"),

    // 광역/연결
    GYEONGUI_JUNGANG(1063, "경의중앙선"),
    JUNGANG(1061, "중앙선"),           // 구 표기(데이터에 존재) - 유지
    AIRPORT(1065, "공항철도"),
    GYEONGCHUN(1067, "경춘선"),
    SUIN_BUNDANG(1075, "수인분당선"),
    SHINBUNDANG(1077, "신분당선"),
    UI_SINSEOL(1091, "우이신설선"),
    SINLIM(1092, "신림선"),
    SEOHAE(1093, "서해선"),
    GYEONGGANG(1081, "경강선"),

    // GTX (A만 실운영)
    GTX_A(1032, "GTX-A"),

    // 인천/경전철
    INCHEON_1(1501, "인천1호선"),
    INCHEON_2(1502, "인천2호선"),
    GIMPO_GOLDLINE(1601, "김포골드라인"),
    UIJEONGBU_LRT(1602, "의정부경전철"),
    EVERLINE(1603, "에버라인");   // 용인경전철

    private final int code;
    private final String displayName;

    MetroLine(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static MetroLine ofCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException(code + " code cannot be null");
        }
        return Arrays.stream(values())
                .filter(line -> line.code == code)
                .findFirst().orElseThrow();
    }

    public static MetroLine ofName(String name) {
        if (name == null) return null;
        String n = normalize(name);
        for (var line : values()) {
            if (normalize(line.displayName).equals(n)) {
                return line;
            }
        }
        return NAME_ALIASES.get(n);
    }

    /** 코드 또는 이름 문자열(예: "1009", "9호선", "9호선(연장)")로 찾기 */
    public static MetroLine fromAny(String codeOrName) {
        if (codeOrName == null) {
            return null;
        }
        String s = codeOrName.trim();
        // 숫자면 코드 우선
        if (s.chars().allMatch(Character::isDigit)) {
            try {
                int c = Integer.parseInt(s);
                MetroLine byCode = ofCode(c);
                if (byCode != null) return byCode;
            } catch (NumberFormatException ignore) {
                throw new IllegalArgumentException(codeOrName + ":" + s + " is not a valid code");
            }
        }
        // 이름/별칭
        return ofName(s);
    }

    /** 코드 → 표시 이름 */
    public static String toDisplayName(Integer code) {
        MetroLine line = ofCode(code);
        return (line != null) ? line.displayName : String.valueOf(code);
    }

    /** 코드/이름이 모두 일치하는지 */
    public static boolean matches(Integer code, String displayName) {
        MetroLine line = ofCode(code);
        return line != null && line.displayName.equals(displayName);
    }

    private static String normalize(String s) {
        return s.replace("\uFEFF","")     // BOM
                .replaceAll("\\s+","")    // 모든 공백 제거
                .trim();
    }

    /** 이름/별칭 → 라인 매핑 테이블 (CSV 표기 대응) */
    // https://t-data.seoul.go.kr/dataprovide/trafficdataviewfile.do?data_id=36
    private static final Map<String, MetroLine> NAME_ALIASES = new HashMap<>() {{
        // 9호선 파생
        put(normalize("9호선(연장)"), LINE_9);
        put(normalize("9호선(연장2)"), LINE_9);

        // 공항철도 표기
        put(normalize("AREX"), AIRPORT);
        put(normalize("공항철도"), AIRPORT);
        put(normalize("공항철도1호선"), AIRPORT);

        // 경의/중앙
        put(normalize("경의중앙선"), GYEONGUI_JUNGANG);
        put(normalize("중앙선"), JUNGANG); // 데이터에 '중앙선' 다수 존재

        // 수인/분당
        put(normalize("분당선"), SUIN_BUNDANG);
        put(normalize("수인선"), SUIN_BUNDANG);
        put(normalize("수인분당선"), SUIN_BUNDANG);

        // 경전철/인천
        put(normalize("의정부선"), UIJEONGBU_LRT);       // 데이터 표기
        put(normalize("의정부경전철"), UIJEONGBU_LRT);
        put(normalize("에버라인"), EVERLINE);
        put(normalize("에버라인선"), EVERLINE);
        put(normalize("용인경전철"), EVERLINE);
        put(normalize("용인 에버라인"), EVERLINE);
        put(normalize("김포골드라인"), GIMPO_GOLDLINE);
        put(normalize("인천1호선"), INCHEON_1);
        put(normalize("인천2호선"), INCHEON_2);

        // 신림/우신
        put(normalize("신림선"), SINLIM);
        put(normalize("우이신설선"), UI_SINSEOL);

        // 7호선(인천) 분기 표기 → 본선으로 귀속
        put(normalize("7호선(인천)"), LINE_7);

        // 8호선 연장 표기
        put(normalize("별내선"), LINE_8);

        // 4호선 분기/연장 표기
        put(normalize("진접선"), LINE_4);
        put(normalize("과천선"), LINE_4);
        put(normalize("안산선"), LINE_4);

        // 3호선 분기
        put(normalize("일산선"), LINE_3);

        // 1호선 분기(코레일 본선들)
        put(normalize("경부선"), LINE_1);
        put(normalize("경원선"), LINE_1);
        put(normalize("경인선"), LINE_1);
        put(normalize("장항선"), LINE_1);

        // 기타 그대로
        put(normalize("경강선"), GYEONGGANG);
        put(normalize("경춘선"), GYEONGCHUN);
        put(normalize("서해선"), SEOHAE);

        // GTX 일반 표기 → A로 귀속(현 운영)
        put(normalize("수도권광역급행철도"), GTX_A);
        put(normalize("수도권 광역급행철도"), GTX_A);

        // 신분당선 연장 표기
        put(normalize("신분당선(연장)"), SHINBUNDANG);
        put(normalize("신분당선(연장2)"), SHINBUNDANG);
    }};
}
