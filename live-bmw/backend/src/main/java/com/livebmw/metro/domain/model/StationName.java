package com.livebmw.metro.domain.model;

public record StationName(String value) {
    public StationName {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("station empty");
    }
}