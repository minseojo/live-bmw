//package com.livebmw.metro.api;
//
//import com.livebmw.metro.api.dto.RouteArrivalsResponse;
//import com.livebmw.metro.application.service.MetroArrivalService;
//import com.livebmw.metro.domain.model.MetroArrival;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/metro")
//@RequiredArgsConstructor
//@Slf4j
//public class MetroController {
//
//    private final MetroArrivalService metroArrivalService;
//
//    @PostMapping("/arrivals/{from}")
//    public MetroArrival arrivalsAlongRoute(@Valid @RequestParam String from) {
//        log.info("arrivalsAlongRoute req={}", from);
//        return metroArrivalService.fetchArrivals(from);
//    }
//}
