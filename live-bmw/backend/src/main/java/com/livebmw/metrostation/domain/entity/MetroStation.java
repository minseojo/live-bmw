package com.livebmw.metrostation.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "metro_station")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
public class MetroStation {
    @Id
    @Column(name = "station_id")
    private String id;

    @Column(name = "station_name", nullable = false)
    private String name;

    @Column(name = "line_id", nullable = false)
    private int lineId;

    @Column(name = "line_name", nullable = false)
    private String lineName;

    @Column(name = "geom", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point geom;

}