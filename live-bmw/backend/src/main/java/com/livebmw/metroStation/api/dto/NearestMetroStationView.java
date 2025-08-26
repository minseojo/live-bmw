package com.livebmw.metroStation.api.dto;

public interface NearestMetroStationView {
    String getStationId();
    String getStationName();
    String getLineId();
    String getLineName();
    Double getDistanceM();
}
