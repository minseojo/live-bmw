package com.livebmw.metro.domain.model;

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
    LINE_9("1009", "9호선");

    private final String code;
    private final String displayName;

    MetroLine(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /** 코드 → enum 매핑 (없으면 null) */
    public static MetroLine of(String code) {
        return Arrays.stream(values())
                .filter(line -> line.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    /** 코드 → 표시명 (없으면 원본 코드 그대로) */
    public static String toDisplayName(String code) {
        MetroLine line = of(code);
        return (line != null) ? line.displayName : code;
    }

    public static boolean matches(String code, String displayName) {
        for (var l : values()) if (l.code.equals(code) && l.displayName.equals(displayName)) return true;
        return false;
    }
}
