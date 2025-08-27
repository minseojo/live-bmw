package com.livebmw.metrostation.application.service;

import com.livebmw.metrostation.api.dto.NearestMetroStationResponse;
import com.livebmw.metrostation.api.dto.NearestMetroStationView;
import com.livebmw.metrostation.domain.repository.MetroStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetroNearestStationService {

    private final MetroStationRepository metroStationRepository;


    public List<NearestMetroStationResponse> find(double lat, double lng, int limit) {
        var rows = metroStationRepository.findNearest(lat, lng, limit);

        // lineName(예: 2호선) 별로 가장 가까운 것만 보존
        Map<String, NearestMetroStationView> byLine = new LinkedHashMap<>();
        for (NearestMetroStationView row : rows) {
            byLine.putIfAbsent(row.getLineName(), row);
        }

        return byLine.values().stream()
                .sorted(Comparator.comparingDouble(NearestMetroStationView::getDistanceM))
                .limit(limit)
                .map(v -> new NearestMetroStationResponse(
                        v.getStationId(),
                        v.getStationName(),
                        String.valueOf(v.getLineId()),
                        v.getLineName(),
                        (int) Math.round(v.getDistanceM())
                ))
                .toList();
    }

}
