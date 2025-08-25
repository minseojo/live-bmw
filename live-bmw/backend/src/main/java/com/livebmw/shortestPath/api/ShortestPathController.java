package com.livebmw.shortestPath.api;

import com.livebmw.common.time.DateTimeUtil;
import com.livebmw.shortestPath.api.dto.ShortestPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livebmw.shortestPath.application.adapter.seoul.api.SeoulShortestPathClient.SearchType;
import com.livebmw.shortestPath.application.service.ShortestPathService;

@RestController
@RequestMapping("/api/shortestPath")
@RequiredArgsConstructor
@Slf4j
public class ShortestPathController {

    private final ShortestPathService shortestPathService;

    @GetMapping
    public ShortestPathResponse get(@RequestParam("from") String departureStationName,
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
}
