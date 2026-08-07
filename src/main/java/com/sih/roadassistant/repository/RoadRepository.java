package com.sih.roadassistant.repository;

import com.sih.roadassistant.model.Road;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoadRepository extends JpaRepository<Road, UUID> {

    // 1. Find the single NEAREST road segment to a given coordinate (point)
    // Uses the PostGIS geometry distance operator '<->' for indexed nearest-neighbor search
    @Query(value = "SELECT * FROM roads ORDER BY geometry <-> :point LIMIT 1", nativeQuery = true)
    Road findNearestRoad(@Param("point") Point point);

    // 2. Find all roads within a certain radius (meters) of a coordinate
    @Query(value = "SELECT * FROM roads WHERE ST_DWithin(geometry, :point, :radiusMeters)", nativeQuery = true)
    List<Road> findRoadsWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);
}