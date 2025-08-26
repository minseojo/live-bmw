package com.livebmw.metroStation.config;

import com.livebmw.metroStation.domain.MetroStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetroStationBootstrapper {

    private final MetroStationRepository stationRepository;

    @Value("${livebmw.metro.station-import.enabled}")
    boolean enabled;

    @Bean
    @Order(1)
    ApplicationRunner importRunner(MetroStationImporter importer,
                                   @Value("${livebmw.metro.station-import.enabled:false}") boolean enabled) {
        return args -> {
            if (!enabled) return;
            importer.importCsv();
        };
    }

    @Bean
    @Order(2)
    ApplicationRunner metroStationImporterRunner() {
        return args -> {
            if (!enabled) {
                log.info("[metro] station bootstrap disabled (JPA mode).");
                return;
            }
            long count = stationRepository.count();
            if (count == 0) {
                log.warn("[metro] metro_station is empty. Provide data via JPA migrations or separate loader.");
            } else {
                log.info("[metro] metro_station rows present: {}", count);
            }
        };
    }
}
