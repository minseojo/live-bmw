package com.livebmw.shortestPath.application.service;

import com.livebmw.metro.api.dto.MetroArrivalResponse;
import com.livebmw.metro.application.service.MetroArrivalService;
import com.livebmw.metro.domain.model.MetroArrival;
import com.livebmw.shortestPath.api.dto.KeyStation;
import com.livebmw.shortestPath.api.dto.ShortedPathResponse;
import com.livebmw.shortestPath.application.adapter.seoul.api.SeoulShortestPathClient.SearchType;
import com.livebmw.shortestPath.domain.ShortestPathPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShortestPathOrchestratorService {

    private final ShortestPathService shortestPathService;
    private final MetroArrivalService metroArrivalService;

    /**
     * 계획을 조회하고, 출발/환승 역에 대한 실시간 도착 정보를 묶어서 반환.
     */
    public ShortedPathResponse findPlanAndArrivals(String departureStationName,
                                                   String arrivalStationName,
                                                   SearchType searchType,
                                                   LocalDateTime when) {
        ShortestPathPlan plan = (when == null)
                ? shortestPathService.findPlan(departureStationName, arrivalStationName, searchType)
                : shortestPathService.findPlan(departureStationName, arrivalStationName, searchType, when);

        List<KeyStation> keyStations = extractKeyStations(plan);

        Map<KeyStation, List<MetroArrivalResponse>> arrivalsByStation = new LinkedHashMap<>();
        for (KeyStation station : keyStations) {
            List<MetroArrival> arrivals = metroArrivalService.fetchArrivals(station.stationName());
            arrivalsByStation.put(
                    station,
                    arrivals.stream()
                            .filter(arrival -> station.direction().equals(arrival.direction()))
                            .map(MetroArrivalResponse::from)
                            .collect(Collectors.toList())
            );
        }

        return new ShortedPathResponse(plan, keyStations, arrivalsByStation);
    }

    /** 출발역 + 환승역들 추출 (도착역 제외, direction 포함) */
    private List<KeyStation> extractKeyStations(ShortestPathPlan plan) {
        if (plan == null || plan.legs() == null || plan.legs().isEmpty()) return List.of();

        List<KeyStation> stations = new ArrayList<>();
        
        // List에서 환승 지점만 추출
        Set<String> processedStations = new HashSet<>();
        
        for (int i = 0; i < plan.legs().size(); i++) {
            ShortestPathPlan.Leg currentLeg = plan.legs().get(i);
            String currentFromName = currentLeg.fromName();
            String currentFromLine = currentLeg.fromLine();
            String currentDirection = currentLeg.direction();
            
            // 첫 번째 leg의 출발역은 항상 추가 (최초 탑승역)
            if (i == 0) {
                String stationKey = currentFromName + ":" + currentDirection;
                stations.add(new KeyStation(currentFromName, currentDirection));
                processedStations.add(stationKey);
                continue;
            }
            
            // 환승 감지: 두 가지 경우
            boolean isTransfer = false;
            String transferDirection = currentDirection;
            
            // 1) 이전 leg의 도착역과 현재 leg의 출발역이 같고 라인이 다른 경우
            ShortestPathPlan.Leg previousLeg = plan.legs().get(i - 1);
            String previousToName = previousLeg.toName();
            String previousToLine = previousLeg.toLine();
            
            if (Objects.equals(currentFromName, previousToName) && 
                !Objects.equals(currentFromLine, previousToLine)) {
                isTransfer = true;
            }
            
            // 2) 현재 leg가 같은 역에서 라인이 바뀌는 환승 leg인 경우 (fromName == toName, fromLine != toLine)
            if (Objects.equals(currentFromName, currentLeg.toName()) && 
                !Objects.equals(currentFromLine, currentLeg.toLine())) {
                isTransfer = true;
                // 환승 leg의 경우 다음 leg의 direction 사용
                if (i + 1 < plan.legs().size()) {
                    transferDirection = plan.legs().get(i + 1).direction();
                }
            }
            
            if (isTransfer) {
                String stationKey = currentFromName + ":" + transferDirection;
                if (!processedStations.contains(stationKey)) {
                    stations.add(new KeyStation(currentFromName, transferDirection));
                    processedStations.add(stationKey);
                }
            }
        }

        return stations;
    }

}


