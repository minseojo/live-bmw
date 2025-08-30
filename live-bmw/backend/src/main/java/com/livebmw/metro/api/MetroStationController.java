package com.livebmw.metro.api;

import com.livebmw.metro.api.dto.NearestMetroStationResponse;
import com.livebmw.metro.application.service.MetroNearestStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metro/stations")
@RequiredArgsConstructor
@Slf4j
public class MetroStationController {

    private final MetroNearestStationService metroNearestStationService;

    @GetMapping("/nearest")
    public ResponseEntity<List<NearestMetroStationResponse>> nearest(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "3") int limit
    ) {
        log.info("nearest stations lat={}, lng={}, limit={}", lat, lng, limit);
        return ResponseEntity.ok(metroNearestStationService.find(lat, lng, Math.min(limit, 10)));
    }
}
