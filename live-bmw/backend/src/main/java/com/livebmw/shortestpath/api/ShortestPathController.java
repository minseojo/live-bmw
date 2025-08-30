package com.livebmw.shortestpath.api;

import com.livebmw.common.time.DateTimeUtil;
import com.livebmw.shortestpath.api.dto.ShortedPathResponse;
import com.livebmw.shortestpath.domain.ShortestPathPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livebmw.shortestpath.application.adapter.seoul.api.SeoulShortestPathClient.SearchType;
import com.livebmw.shortestpath.application.service.ShortestPathService;
import com.livebmw.shortestpath.application.service.ShortestPathOrchestratorService;

@RestController
@RequestMapping("/api/shortest-path")
@RequiredArgsConstructor
@Slf4j
public class ShortestPathController {

    private final ShortestPathService shortestPathService;
    private final ShortestPathOrchestratorService orchestratorService;

    @GetMapping
    public ShortestPathPlan get(@RequestParam("from") String departureStationName,
                                @RequestParam("to") String arrivalStationName,
                                @RequestParam(value = "searchType", defaultValue = "duration") SearchType searchType,
                                @RequestParam(value = "when", required = false) String when) {
        log.info("find shortest path {} to {}\nwhen: {}", departureStationName, arrivalStationName, when);

        if (when == null) {
            return shortestPathService.findPlan(departureStationName, arrivalStationName, searchType);
        } else {
            return shortestPathService.findPlan(departureStationName, arrivalStationName, searchType, DateTimeUtil.parseKst(when));
        }
    }

    @GetMapping("/with-arrivals")
    public ResponseEntity<ShortedPathResponse> getWithArrivals(@RequestParam("from") String departureStationName,
                                                               @RequestParam("to") String arrivalStationName,
                                                               @RequestParam(value = "searchType", defaultValue = "duration") SearchType searchType,
                                                               @RequestParam(value = "when", required = false) String when) {
        if (!isOnlyComposedHangulWithDigit(departureStationName.trim()) || !isOnlyComposedHangulWithDigit(arrivalStationName.trim())) {
            return ResponseEntity.badRequest().build();
        }

        log.info("{} to {} - {}", departureStationName, arrivalStationName, searchType);
        if (when == null) {
            return ResponseEntity.ok(orchestratorService.findPlanAndArrivals(departureStationName, arrivalStationName, searchType, null));
        } else {
            return ResponseEntity.ok(orchestratorService.findPlanAndArrivals(departureStationName, arrivalStationName, searchType, DateTimeUtil.parseKst(when)));
        }
    }

    public static boolean isOnlyComposedHangulWithDigit(String s) {
        return s != null && !s.isEmpty() && s.matches("^[가-힣0-9()]+$");

    }

}
