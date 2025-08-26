package com.livebmw.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class GeometryFactory4326 {

    private final GeometryFactory geometryFactory;

    public GeometryFactory4326() {
        var cs = new PrecisionModel();
        this.geometryFactory = new GeometryFactory(cs, 4326);
    }
    public Point point(double lon, double lat) {
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}
