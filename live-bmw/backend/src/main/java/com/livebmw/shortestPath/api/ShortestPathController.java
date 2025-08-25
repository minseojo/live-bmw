package com.livebmw.shortestPath.api;

import com.livebmw.common.time.DateTimeUtil;
import com.livebmw.shortestPath.api.dto.ShortedPathResponse;
import com.livebmw.shortestPath.domain.ShortestPathPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livebmw.shortestPath.application.adapter.seoul.api.SeoulShortestPathClient.SearchType;
import com.livebmw.shortestPath.application.service.ShortestPathService;
import com.livebmw.shortestPath.application.service.ShortestPathOrchestratorService;

@RestController
@RequestMapping("/api/shortestPath")
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
    public ShortedPathResponse getWithArrivals(@RequestParam("from") String departureStationName,
                                               @RequestParam("to") String arrivalStationName,
                                               @RequestParam(value = "searchType", defaultValue = "duration") SearchType searchType,
                                               @RequestParam(value = "when", required = false) String when) {
        if (when == null) {
            return orchestratorService.findPlanAndArrivals(departureStationName, arrivalStationName, searchType, null);
        } else {
            return orchestratorService.findPlanAndArrivals(departureStationName, arrivalStationName, searchType, DateTimeUtil.parseKst(when));
        }
    }
}
