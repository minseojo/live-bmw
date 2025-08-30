package com.livebmw.metro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livebmw.metro.station-import")
public record MetroStationImportProps(
        boolean enabled,
        String infoCsv,
        String coordCsv
) {}
