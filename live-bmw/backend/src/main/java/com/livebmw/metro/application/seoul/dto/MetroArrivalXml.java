package com.livebmw.metro.application.seoul.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "realtimeStationArrival")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetroArrivalXml {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "row")
    public List<Row> rows;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        @JacksonXmlProperty(localName = "subwayId")  public Integer lineId;   // 1002 = 2호선, 지하철호선ID
        @JacksonXmlProperty(localName = "updnLine")  public String updnLine;   // 상행/하행/내선/외선
        @JacksonXmlProperty(localName = "trainLineNm") public String trainLineNm; // 성수행 - 봉천방면
        @JacksonXmlProperty(localName = "statnId")   public String statnId;     // 지하철역ID, 예: 1002000228
        @JacksonXmlProperty(localName = "statnNm")   public String statnNm;     // 지하철역명  예: 봉천
        @JacksonXmlProperty(localName = "btrainSttus") public String btrainSttus; // 일반/급행 등
        @JacksonXmlProperty(localName = "barvlDt")   public Integer barvlDt;   // 열차도착예정시간(초)
        @JacksonXmlProperty(localName = "recptnDt")  public String recptnDt;   // 열차도착정보를 생성한 시각, recptnDt, yyyy-MM-dd HH:mm:ss
        @JacksonXmlProperty(localName = "arvlCd")    public String arvlCd;     // 도착코드, (0:진입, 1:도착, 2:출발, 3:전역출발, 4:전역진입, 5:전역도착, 99:운행중)
        @JacksonXmlProperty(localName = "lstcarAt")  public String lstcarAt;    // 막차여부, (1:막차, 0:아님)
//        @JacksonXmlProperty(localName = "btrainNo")  public String btrainNo;   // 종착지하철역ID
//        @JacksonXmlProperty(localName = "bstatnNm")  public String bstatnNm;   // 종착지하철역명
//        @JacksonXmlProperty(localName = "arvlMsg2")  public String arvlMsg2;   // "2분 20초 후" 등, 첫번째도착메세지 (도착, 출발 , 진입 등)
//        @JacksonXmlProperty(localName = "arvlMsg3")  public String arvlMsg3;   // 행선/상태, 두번째도착메세지 (종합운동장 도착, 12분 후 (광명사거리) 등)
    }
}
