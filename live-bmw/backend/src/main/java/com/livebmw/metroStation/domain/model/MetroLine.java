package com.livebmw.metroStation.domain.model;

import java.util.Arrays;

public enum MetroLine {
    LINE_1("1001", "1호선"),
    LINE_2("1002", "2호선"),
    LINE_3("1003", "3호선"),
    LINE_4("1004", "4호선"),
    LINE_5("1005", "5호선"),
    LINE_6("1006", "6호선"),
    LINE_7("1007", "7호선"),
    LINE_8("1008", "8호선"),
    LINE_9("1009", "9호선"),

    JUNGANG("1061", "중앙선"),
    GYEONGUI_JUNGANG("1063", "경의중앙선"),
    AIRPORT("1065", "공항철도"),
    GYEONGCHUN("1067", "경춘선"),
    SUIN_BUNDANG("1075", "수인분당선"),
    SHINBUNDANG("1077", "신분당선"),
    WUI_SINSEOL("1092", "우이신설선"),
    SEOHAE("1093", "서해선"),
    GYEONGGANG("1081", "경강선"),
    GTX_A("1032", "GTX-A");

    private final String code;
    private final String displayName;

    MetroLine(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static MetroLine of(String code) {
        return Arrays.stream(values())
                .filter(line -> line.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String toDisplayName(String code) {
        MetroLine line = of(code);
        return (line != null) ? line.displayName : code;
    }

    public static boolean matches(String code, String displayName) {
        for (var line : values()) {
            if (line.code.equals(code) && line.displayName.equals(displayName)) return true;
        }
        return false;
    }
}
