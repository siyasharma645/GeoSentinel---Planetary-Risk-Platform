package com.geosentinel.climate.service;
import com.geosentinel.climate.model.ClimateMetric;
import com.geosentinel.climate.repository.ClimateMetricRepository;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.List;
@Slf4j @Service @RequiredArgsConstructor
public class ClimateService {
    private final ClimateMetricRepository repo;
    public Page<ClimateMetric> byType(String type,Pageable p){return repo.findByMetricTypeOrderByRecordedAtDesc(type,p);}
    public List<ClimateMetric> byCountry(String cc){return repo.findByCountryCodeOrderByRecordedAtDesc(cc);}
    public List<ClimateMetric> anomalies(double threshold){return repo.findAnomalies(threshold);}
    @Transactional public ClimateMetric record(ClimateMetric m){m.setRecordedAt(Instant.now());return repo.save(m);}
}
