package com.geosentinel.risk.repository;

import com.geosentinel.risk.model.LocationRiskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LocationRiskReportRepository extends JpaRepository<LocationRiskReport, UUID> {

    @Query("""
        SELECT r FROM LocationRiskReport r
        WHERE ABS(r.lat - :lat) < 0.05
          AND ABS(r.lon - :lon) < 0.05
          AND r.expiresAt > :now
        ORDER BY r.generatedAt DESC
        LIMIT 1
        """)
    Optional<LocationRiskReport> findValidByCoords(Double lat, Double lon, Instant now);

    @Query("""
        SELECT r FROM LocationRiskReport r
        WHERE r.countryCode = :countryCode
          AND r.expiresAt > :now
        ORDER BY r.generatedAt DESC
        LIMIT 1
        """)
    Optional<LocationRiskReport> findValidByCountryCode(String countryCode, Instant now);
}
