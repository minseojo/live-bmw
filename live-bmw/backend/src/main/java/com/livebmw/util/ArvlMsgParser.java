package com.livebmw.util;

import java.util.regex.*;

public final class ArvlMsgParser {
    private ArvlMsgParser() {}

    private static final Pattern MIN_SEC = Pattern.compile("(\\d+)\\s*분\\s*(\\d+)\\s*초\\s*후");
    private static final Pattern MIN_ONLY = Pattern.compile("(\\d+)\\s*분\\s*후");
    private static final Pattern SEC_ONLY = Pattern.compile("(\\d+)\\s*초\\s*후");

    /** 실패 시 null */
    public static Integer parseSeconds(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        if (s.contains("당역 도착") || s.contains("진입") || s.contains("도착") || s.contains("곧 도착")) return 0;
        if (s.contains("전역 출발")) return 60; // 휴리스틱

        Matcher m = MIN_SEC.matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1))*60 + Integer.parseInt(m.group(2));

        m = MIN_ONLY.matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1))*60;

        m = SEC_ONLY.matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1));

        return null;
    }
}
