// application/service/MetroRouteArrivalService.java
package com.livebmw.metro.application.service;

import com.livebmw.metro.api.dto.MetroArrivalRequest;
import com.livebmw.metro.api.dto.MetroArrivalResponse;
import com.livebmw.metro.api.dto.RouteArrivalsResponse;
import com.livebmw.metro.domain.model.MetroArrival;
import com.livebmw.metro.domain.model.MetroDirection;
import com.livebmw.metro.domain.model.MetroLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.livebmw.metro.domain.model.MetroDirection.INNER;

// MetroRouteArrivalService.java

@Service
@RequiredArgsConstructor
@Slf4j
public class MetroRouteArrivalService {

    private final MetroArrivalService arrivalService;
    private final DirectionResolver directionResolver;

    // ★ 우리 topology에서 "정방향"이 어떤 라벨인지 명시
    //   2호선(1002): 이 프로젝트의 LINES 순서는 '외선'에 해당한다고 가정 (현재 증상 기준)
    private static final Map<String, MetroDirection> FORWARD_DIR = Map.of(
            "1002", MetroDirection.OUTER
    );

    public RouteArrivalsResponse arrivalsAlongRoute(MetroArrivalRequest req) {
        final String origin = req.originStation().trim();
        final String dest   = req.destinationStation().trim();
        final String lineId = requireLineId(req);

        final MetroDirection logicalDir = directionResolver.resolve(lineId, origin, dest);

        // ★ 논리적 진행방향을 topology 기준 라벨로 보정
        // todo::
//        final MetroDirection displayDir = normalizeByTopology(lineId, logicalDir);
        var displayDir = INNER;
        final String nextStation  = directionResolver.nextStationToward(lineId, origin, dest);
        log.info("logical={}, display={}, nextStation={}", logicalDir, displayDir, nextStation);

        final List<MetroArrival> arrivalsAtOrigin = arrivalService.fetchArrivals(origin);

        final List<MetroArrival> filtered = arrivalsAtOrigin.stream()
                .filter(a -> matchesDirection(displayDir, a.directionLabel()))
                .limit(req.limitOrDefault())
                .toList();

        return new RouteArrivalsResponse(
                lineId,
                MetroLine.toDisplayName(lineId),
                origin,
                dest,
                dirToKo(displayDir),        // ★ 응답도 보정된 라벨로
                nextStation,
                filtered.stream().map(MetroArrivalResponse::from).toList()
        );
    }

    private MetroDirection normalizeByTopology(String lineId, MetroDirection logical) {
        // 원형 노선 외에는 그대로 반환
        if (!"1002".equals(lineId)) return logical;

        // topology의 "정방향" 라벨을 읽음 (여기선 OUTER)
        final MetroDirection forward = FORWARD_DIR.getOrDefault(lineId, INNER);

        // logical이 "정방향"이면 forward, 아니면 그 반대 라벨을 돌려줌
        // DirectionResolver가 forward/backward를 구분해 주지 않는다면,
        // INNER/OUTER일 때만 스왑 로직을 적용
        if (logical == INNER || logical == MetroDirection.OUTER) {
            return (logical == forward) ? forward : opposite(forward);
        }
        return logical; // UP/DOWN은 그대로
    }

    private MetroDirection opposite(MetroDirection d) {
        return switch (d) {
            case INNER -> MetroDirection.OUTER;
            case OUTER -> INNER;
            case UP    -> MetroDirection.DOWN;
            case DOWN  -> MetroDirection.UP;
        };
    }

    private boolean matchesDirection(MetroDirection expected, String updnRaw) {
        final String updn = updnRaw == null ? "" : updnRaw.trim();
        return switch (expected) {
            case UP    -> "상행".equals(updn);
            case DOWN  -> "하행".equals(updn);
            case INNER -> "내선".equals(updn);
            case OUTER -> "외선".equals(updn);
        };
    }

    private String dirToKo(MetroDirection dir) {
        return switch (dir) {
            case UP -> "상행"; case DOWN -> "하행"; case INNER -> "내선"; case OUTER -> "외선";
        };
    }

    private String requireLineId(MetroArrivalRequest req) {
        if (req.lineId() == null || req.lineId().isBlank())
            throw new IllegalArgumentException("lineId 필요 (환승 미해결 상태)");
        return req.lineId();
    }
}
