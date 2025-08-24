package com.livebmw.metro.application.adapter.seoul.dto;

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
        @JacksonXmlProperty(localName = "subwayId")  public String subwayId;   // 1002 = 2호선
        @JacksonXmlProperty(localName = "updnLine")  public String updnLine;   // 상행/하행/내선/외선
        @JacksonXmlProperty(localName = "trainLineNm") public String trainLineNm; // 성수행 - 봉천방면
        @JacksonXmlProperty(localName = "statnId")   public String statnId;
        @JacksonXmlProperty(localName = "statnNm")   public String statnNm;
        @JacksonXmlProperty(localName = "btrainSttus") public String btrainSttus; // 일반/급행 등
        @JacksonXmlProperty(localName = "barvlDt")   public Integer barvlDt;   // 남은초(생성시각 기준)
        @JacksonXmlProperty(localName = "btrainNo")  public String btrainNo;   // 열차번호
        @JacksonXmlProperty(localName = "bstatnNm")  public String bstatnNm;   // 기준상 “전역”명
        @JacksonXmlProperty(localName = "recptnDt")  public String recptnDt;   // yyyy-MM-dd HH:mm:ss
        @JacksonXmlProperty(localName = "arvlMsg2")  public String arvlMsg2;   // "2분 20초 후" 등
        @JacksonXmlProperty(localName = "arvlMsg3")  public String arvlMsg3;   // 행선/상태
        @JacksonXmlProperty(localName = "arvlCd")    public String arvlCd;     // 상태코드(참고용)
    }
}
