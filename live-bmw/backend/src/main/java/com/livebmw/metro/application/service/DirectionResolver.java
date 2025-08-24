package com.livebmw.metro.application.service;

import com.livebmw.metro.domain.model.LineTopology;
import com.livebmw.metro.domain.model.MetroDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DirectionResolver {

    private final LineTopology topology;

    public MetroDirection resolve(String lineId, String origin, String destination) {
        List<String> seq = topology.stationsOf(lineId);
        int oi = seq.indexOf(origin), di = seq.indexOf(destination);
        if (oi < 0 || di < 0) throw new IllegalArgumentException("역이 라인에 없음");
        if (topology.isCircular(lineId)) {
            int n = seq.size();
            int cw  = (di - oi + n) % n;   // 시계
            int ccw = (oi - di + n) % n;   // 반시계
            return (cw <= ccw) ? MetroDirection.INNER : MetroDirection.OUTER;
        } else {
            return (di > oi) ? MetroDirection.UP : MetroDirection.DOWN;
        }
    }

    /** 출발→도착 방향으로 한 칸 이동한 '다음 역' */
    public String nextStationToward(String lineId, String origin, String destination) {
        List<String> seq = topology.stationsOf(lineId);
        int oi = seq.indexOf(origin), di = seq.indexOf(destination);
        if (oi < 0 || di < 0 || seq.isEmpty()) throw new IllegalArgumentException("역이 라인에 없음");

        if (topology.isCircular(lineId)) {
            int n = seq.size();
            int cw  = (di - oi + n) % n;
            int ccw = (oi - di + n) % n;
            int nextIdx = (cw <= ccw) ? (oi + 1) % n : (oi - 1 + n) % n;
            return seq.get(nextIdx);
        } else {
            int step = (di > oi) ? +1 : -1;
            int nextIdx = oi + step;
            if (nextIdx < 0 || nextIdx >= seq.size()) throw new IllegalStateException("다음 역 없음");
            return seq.get(nextIdx);
        }
    }
}
