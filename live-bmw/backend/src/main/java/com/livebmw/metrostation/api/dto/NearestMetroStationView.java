package com.livebmw.metrostation.api.dto;

public interface NearestMetroStationView {
    String getStationId();
    String getStationName();
    String getLineId();
    String getLineName();
    Double getDistanceM();
}
