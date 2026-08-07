package com.sih.roadassistant.service;

import com.sih.roadassistant.dto.RouteRequest;
import com.sih.roadassistant.util.GeometryUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutingService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calculates up to 3 alternative routes from OSRM, scores them individually 
     * based on spatial hazard databases (potholes, accidents, closed gates), and 
     * returns them.
     */
    public Map<String, Object> calculateSafeRoute(RouteRequest request) {
        double startLat = request.getStartLocation().getLatitude();
        double startLng = request.getStartLocation().getLongitude();
        double endLat = request.getEndLocation().getLatitude();
        double endLng = request.getEndLocation().getLongitude();

        // 1. Query the Free OSRM Engine with alternatives=true to get multiple route paths
        String osrmUrl = String.format(
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?geometries=geojson&overview=full&alternatives=true",
                startLng, startLat, endLng, endLat
        );

        try {
            String jsonResponse = restTemplate.getForObject(osrmUrl, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routesArray = root.path("routes");

            List<Map<String, Object>> scoredRoutes = new ArrayList<>();

            for (int i = 0; i < routesArray.size(); i++) {
                JsonNode route = routesArray.get(i);
                double distanceMeters = route.path("distance").asDouble();
                double durationSeconds = route.path("duration").asDouble();

                // Extract path geometry coordinates
                JsonNode coordinatesNode = route.path("geometry").path("coordinates");
                List<Coordinate> jtsCoordinates = new ArrayList<>();
                for (JsonNode pointNode : coordinatesNode) {
                    double lng = pointNode.get(0).asDouble();
                    double lat = pointNode.get(1).asDouble();
                    jtsCoordinates.add(new Coordinate(lng, lat));
                }

                // Convert to a JTS LineString geometry representing the route path
                LineString routeGeometry = GeometryUtils.createLineString(jtsCoordinates);

                // 2. Perform Spatial Analysis using PostGIS on this path
                int potholeCount = countHazardsNearRoute(routeGeometry, "potholes", "coordinate");
                int closedGateCount = countClosedRailwayGatesNearRoute(routeGeometry);
                int accidentCount = countHazardsNearRoute(routeGeometry, "alerts", "coordinate", "alert_type = 'ACCIDENT'");

                // 3. Compute dynamic safety score for this route
                double baseSafety = 100.0;
                double safetyReduction = (potholeCount * 5.0) + (closedGateCount * 30.0) + (accidentCount * 15.0);
                double safetyScore = Math.max(0.0, baseSafety - safetyReduction);

                // Build route result map
                Map<String, Object> routeMap = new HashMap<>();
                routeMap.put("routeIndex", i);
                routeMap.put("distanceMeters", distanceMeters);
                routeMap.put("durationSeconds", durationSeconds + (closedGateCount * 600)); // Add 10-minute penalty for closed gate delays
                routeMap.put("safetyScore", safetyScore);
                routeMap.put("potholesCount", potholeCount);
                routeMap.put("closedGatesCount", closedGateCount);
                routeMap.put("accidentCount", accidentCount);
                routeMap.put("routeGeometry", routeGeometry.toString()); // WKT format: "LINESTRING(lng lat, lng lat)"

                scoredRoutes.add(routeMap);
            }

            // Return a list of all alternative routes scored individually
            Map<String, Object> response = new HashMap<>();
            response.put("routes", scoredRoutes);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Routing calculation failed: " + e.getMessage(), e);
        }
    }

    // Helper method to count potholes or generic hazards intersecting the route buffer
    private int countHazardsNearRoute(LineString routeGeom, String tableName, String geomColumnName, String... extraConditions) {
        String queryStr = String.format(
                "SELECT COUNT(*) FROM %s WHERE ST_DWithin(%s, ST_GeomFromText(:wkt, 4326), 0.00015)", // ~15 meters in degrees
                tableName, geomColumnName
        );
        if (extraConditions.length > 0) {
            queryStr += " AND " + extraConditions[0];
        }

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("wkt", routeGeom.toString());
        return ((Number) query.getSingleResult()).intValue();
    }

    // Custom check specifically for CLOSED railway gates on the route
    private int countClosedRailwayGatesNearRoute(LineString routeGeom) {
        String queryStr = "SELECT COUNT(*) FROM alerts " +
                "WHERE alert_type = 'RAILWAY_GATE' AND status = 'CLOSED' AND is_active = true " +
                "AND ST_DWithin(coordinate, ST_GeomFromText(:wkt, 4326), 0.00015)";
        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("wkt", routeGeom.toString());
        return ((Number) query.getSingleResult()).intValue();
    }
}