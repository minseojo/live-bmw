package com.livebmw.metro.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(
        name = "metro_station",
        indexes = {
                @Index(name = "ix_station_name", columnList = "station_name"),
                @Index(name = "ix_line_id", columnList = "line_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MetroStation {

    @Id
    @Column(name = "station_id")
    private Integer stationId;

    /** MetroLine의 PK(=line_id) 참조 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "line_id", nullable = false)
    private MetroLine line;

    @Column(name = "station_name", nullable = false, length = 60)
    private String stationName;

    @Column(name = "address", length = 200)
    private String address;

    /** 위치 정보 (WGS84 경위도 좌표) */
    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point geom;

    @Column(name = "prev_station_id")
    private Integer prevStationId;

    @Column(name = "next_station_id")
    private Integer nextStationId;
}