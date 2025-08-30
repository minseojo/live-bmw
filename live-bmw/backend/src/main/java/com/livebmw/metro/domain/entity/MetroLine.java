package com.livebmw.metro.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metro_line")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MetroLine {

    /** 서울/수도권 라인 고유 코드 (예: 1002) */
    @Id
    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    /** 표시 이름 (예: "2호선") */
    @Column(name = "line_name", nullable = false, length = 50)
    private String lineName;
}
