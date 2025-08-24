//package com.livebmw.metro.application.adapter.seoul;
//
//import com.livebmw.metro.application.adapter.seoul.dto.MetroArrivalXml;
//import com.livebmw.metro.domain.model.MetroArrival;
//import com.livebmw.util.ArvlMsgParser;
//
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//
//public final class SeoulXmlMapper {
//    private SeoulXmlMapper() {}
//
//    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
//    private static final DateTimeFormatter RECPTN_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
//
//    /** XML Row → MetroArrival (arvlMsg2 파싱 + barvlDt 보조 + 수신시각 처리) */
//    public static MetroArrival toDomain(MetroArrivalXml.Row r, Instant fallbackNow) {
//        // 1) etaSeconds: arvlMsg2 파싱 우선, 실패 시 barvlDt(Integer) 보조
//        Integer parsed = ArvlMsgParser.parseSeconds(r.arvlMsg2);
//        int barvl = r.barvlDt != null ? r.barvlDt : 0;
//        int eta = Math.max(0, parsed != null ? parsed : barvl);
//
//        // 2) receivedAt: recptnDt(서울시 제공) → ISO, 없거나 파싱 실패하면 서버 now 사용
//        Instant recvd = parseRecptn(r.recptnDt, fallbackNow);
//
//        return new MetroArrival(
//                r.subwayId,           // lineId
//                r.updnLine,           // directionLabel
//                r.trainLineNm,        // trainLineSummary
//                r.btrainNo,           // trainNumber
//                r.arvlMsg2,           // message
//                r.arvlMsg3,           // messageSub
//                eta,                  // etaSeconds
//                ISO.format(recvd)     // receivedAt(ISO)
//        );
//        // 필요 시 statnNm, bstatnNm 등은 별도 응답 DTO에 추가하세요.
//    }
//
//    private static Instant parseRecptn(String recptnDt, Instant def) {
//        if (recptnDt == null || recptnDt.isBlank()) return def;
//        try {
//            LocalDateTime ldt = LocalDateTime.parse(recptnDt.trim(), RECPTN_FMT);
//            return ldt.atZone(SEOUL).toInstant();
//        } catch (DateTimeParseException e) {
//            return def;
//        }
//    }
//}
