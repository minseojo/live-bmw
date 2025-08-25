package com.livebmw.common.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private DateTimeUtil() {}

    /** 서비스 기본 타임존 (KST) */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 기본 포맷: yyyy-MM-dd HH:mm:ss */
    public static final DateTimeFormatter YMD_HMS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** "yyyy-MM-dd HH:mm:ss" 포맷 문자열을 LocalDateTime으로 파싱 (KST 기준) */
    public static LocalDateTime parseKst(String text) {
        return LocalDateTime.parse(text, YMD_HMS);
    }

    /** LocalDateTime을 기본 포맷으로 문자열화 */
    public static String formatKst(LocalDateTime dt) {
        return dt.format(YMD_HMS);
    }
}
