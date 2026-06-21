package com.geosentinel.alert.repository;
import com.geosentinel.alert.model.*; import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface AlertRepository extends JpaRepository<Alert,UUID> {
    Page<Alert> findByStatusOrderByCreatedAtDesc(String status, Pageable p);
    List<Alert> findByCountryCodeAndStatusOrderByCreatedAtDesc(String cc, String status);
    List<Alert> findByLevelInAndStatusOrderByCreatedAtDesc(List<AlertLevel> levels, String status);
    long countByStatus(String status);
}
