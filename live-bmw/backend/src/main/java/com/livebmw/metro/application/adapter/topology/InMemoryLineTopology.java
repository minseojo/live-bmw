package com.livebmw.metro.application.adapter.topology;

import com.livebmw.metro.domain.model.LineTopology;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Primary // 기본 구현으로 사용
public class InMemoryLineTopology implements LineTopology {

    // lineId → 정방향 역 순서
    private static final Map<String, List<String>> LINES = Map.of(
            // 2호선(일부 구간 샘플)
            "1002", List.of(
                    "구로디지털단지","대림","신대방","신림","봉천","서울대입구","낙성대",
                    "사당","방배","서초","교대","강남","역삼","선릉","삼성","종합운동장"
            )
    );

    // 원형 노선
    private static final Set<String> CIRCULAR = Set.of("1002"); // 2호선

    @Override
    public List<String> stationsOf(String lineId) {
        return LINES.getOrDefault(lineId, List.of());
    }

    @Override
    public boolean isCircular(String lineId) {
        return CIRCULAR.contains(lineId);
    }
}
