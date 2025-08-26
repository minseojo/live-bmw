package com.livebmw.metroStation.domain;

import com.livebmw.metroStation.api.dto.NearestMetroStationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetroStationRepository extends JpaRepository<MetroStation, String> {

    @Query(value = """
        SELECT
            station_id                    AS stationId,
            station_name                  AS stationName,
            line_id                       AS lineId,
            line_name                     AS lineName,
            ST_DistanceSphere(
                geom,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
            )                             AS distanceM
        FROM metro_station
        ORDER BY geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
        LIMIT :limit
        """, nativeQuery = true)

    List<NearestMetroStationView> findNearest(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("limit") int limit
    );
}
