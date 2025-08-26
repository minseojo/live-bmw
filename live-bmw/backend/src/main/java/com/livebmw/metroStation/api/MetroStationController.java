package com.livebmw.metroStation.api;

import com.livebmw.metroStation.api.dto.NearestMetroStationResponse;
import com.livebmw.metroStation.application.MetroNearestStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metro/stations")
@RequiredArgsConstructor
@Slf4j
public class MetroStationController {

    private final MetroNearestStationService metroNearestStationService;

    @GetMapping("/nearest")
    public List<NearestMetroStationResponse> nearest(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3") int limit
    ) {
        log.info("nearest stations lat={}, lng={}, limit={}", lat, lng, limit);
        return metroNearestStationService.find(lat, lng, Math.min(limit, 10));
    }
}
