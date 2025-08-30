package com.livebmw.metro.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class MetroShortestPath {

    @Id
    @Column(name = "shortest_path_id")
    public Long id;

    @ManyToMany
    @JoinTable(
            name = "shortest_path_stations",
            joinColumns = @JoinColumn(name = "shortest_path_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private List<MetroStation> paths;

}
