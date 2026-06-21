package com.geosentinel.climate.repository;
import com.geosentinel.climate.model.ClimateMetric;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*;
import java.util.List; import java.util.UUID;
public interface ClimateMetricRepository extends JpaRepository<ClimateMetric,UUID> {
    Page<ClimateMetric> findByMetricTypeOrderByRecordedAtDesc(String type, Pageable p);
    List<ClimateMetric> findByCountryCodeOrderByRecordedAtDesc(String cc);
    @Query("SELECT c FROM ClimateMetric c WHERE c.anomaly IS NOT NULL AND ABS(c.anomaly) > :threshold ORDER BY ABS(c.anomaly) DESC")
    List<ClimateMetric> findAnomalies(double threshold);
}
