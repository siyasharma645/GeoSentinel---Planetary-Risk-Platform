package com.geosentinel.disaster.repository;
import com.geosentinel.disaster.model.*; import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*; import java.util.List; import java.util.UUID;
public interface DisasterRepository extends JpaRepository<Disaster,UUID> {
    @Query("SELECT d FROM Disaster d WHERE d.status IN ('ACTIVE','ESCALATING','CRITICAL','WARNING') ORDER BY d.severity DESC NULLS LAST,d.startedAt DESC")
    List<Disaster> findAllActive();
    Page<Disaster> findByCountryCodeIgnoreCaseOrderByStartedAtDesc(String cc,Pageable p);
    Page<Disaster> findByTypeIgnoreCaseOrderBySeverityDesc(String type,Pageable p);
    @Query("SELECT d FROM Disaster d WHERE CAST(d.lat AS double) BETWEEN :latMin AND :latMax AND CAST(d.lon AS double) BETWEEN :lonMin AND :lonMax AND d.status IN ('ACTIVE','ESCALATING','CRITICAL')")
    List<Disaster> findInBox(double latMin,double latMax,double lonMin,double lonMax);
    boolean existsByExternalId(String id);
}
