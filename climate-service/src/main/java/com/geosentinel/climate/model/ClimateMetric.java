package com.geosentinel.climate.model;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="climate_metrics") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClimateMetric {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String metricType;
    private String region, countryCode;
    private BigDecimal lat, lon;
    @Column(nullable=false) private BigDecimal value;
    private String unit;
    private BigDecimal anomaly;
    @Column(nullable=false) private Instant recordedAt=Instant.now();
    private String source;
}
