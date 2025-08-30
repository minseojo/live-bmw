package com.livebmw.metro.application.service;

import com.livebmw.metro.api.dto.NearestMetroStationResponse;
import com.livebmw.metro.api.dto.NearestMetroStationView;
import com.livebmw.metro.domain.repository.MetroStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetroNearestStationService {

    private final MetroStationRepository metroStationRepository;


    public List<NearestMetroStationResponse> find(double lat, double lng, int limit) {
        var rows = metroStationRepository.findNearest(lat, lng, limit);
        for (var row : rows) {
            log.info(row.toString());
        }

        return rows.stream()
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
