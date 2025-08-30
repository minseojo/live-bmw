package com.livebmw.metro.application.init;

import com.livebmw.metro.domain.entity.MetroLine;
import com.livebmw.metro.domain.repository.MetroLineRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetroLineDataInitializer {

    private final MetroLineRepository metroLineRepository;

    @PostConstruct
    public void init() {
        if (metroLineRepository.count() == 0) {
            metroLineRepository.save(MetroLine.builder().lineId(1001).lineName("1호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1002).lineName("2호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1003).lineName("3호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1004).lineName("4호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1005).lineName("5호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1006).lineName("6호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1007).lineName("7호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1008).lineName("8호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1009).lineName("9호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1032).lineName("GTX-A").build());
            metroLineRepository.save(MetroLine.builder().lineId(1061).lineName("중앙선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1063).lineName("경의중앙선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1065).lineName("공항철도").build());
            metroLineRepository.save(MetroLine.builder().lineId(1067).lineName("경춘선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1069).lineName("인천1호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1071).lineName("수인선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1075).lineName("수인분당선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1077).lineName("신분당선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1078).lineName("인천2호선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1079).lineName("의정부").build());
            metroLineRepository.save(MetroLine.builder().lineId(1080).lineName("에버라인").build());
            metroLineRepository.save(MetroLine.builder().lineId(1081).lineName("경강선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1091).lineName("자기부상선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1092).lineName("우이신설선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1093).lineName("서해선").build());
            metroLineRepository.save(MetroLine.builder().lineId(1094).lineName("신림선").build());
        }
    }
}

