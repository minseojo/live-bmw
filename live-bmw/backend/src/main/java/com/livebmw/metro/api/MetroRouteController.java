package com.livebmw.metro.api;

import com.livebmw.metro.api.dto.MetroArrivalRequest;
import com.livebmw.metro.api.dto.MetroArrivalResponse;
import com.livebmw.metro.api.dto.RouteArrivalsResponse;
import com.livebmw.metro.application.service.MetroRouteArrivalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metro")
@RequiredArgsConstructor
@Slf4j
public class MetroRouteController {

    private final MetroRouteArrivalService service;

    @PostMapping("/arrivals/route")
    public RouteArrivalsResponse arrivalsAlongRoute(@Valid @RequestBody MetroArrivalRequest request) {
        log.info("arrivalsAlongRoute req={}", request);
        return service.arrivalsAlongRoute(request);
    }
}
