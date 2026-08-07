package com.sih.roadassistant.repository;

import com.sih.roadassistant.model.Pothole;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PotholeRepository extends JpaRepository<Pothole, UUID> {

    List<Pothole> findByRoadId(UUID roadId);

    // Find all potholes within a specific radius of a user's location
    @Query(value = "SELECT * FROM potholes WHERE ST_DWithin(coordinate, :point, :radiusMeters)", nativeQuery = true)
    List<Pothole> findPotholesWithinRadius(@Param("point") Point point, @Param("radiusMeters") double radiusMeters);
}