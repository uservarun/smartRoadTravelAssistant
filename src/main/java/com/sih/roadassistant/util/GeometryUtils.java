package com.sih.roadassistant.util;

import org.locationtech.jts.geom.*;
import java.util.List;

public class GeometryUtils {

    private static final int SRID = 4326; // Standard GPS coordinate system (WGS84)
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    public static Point createPoint(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static LineString createLineString(List<Coordinate> coordinates) {
        Coordinate[] coordArray = coordinates.toArray(new Coordinate[0]);
        return GEOMETRY_FACTORY.createLineString(coordArray);
    }
}