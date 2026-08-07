package com.sih.roadassistant.repository;

import com.sih.roadassistant.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
}
