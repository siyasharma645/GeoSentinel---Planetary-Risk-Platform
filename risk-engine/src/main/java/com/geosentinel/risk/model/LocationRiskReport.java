package com.geosentinel.risk.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant; import java.util.Map; import java.util.UUID;
@Entity @Table(name="location_risk_reports") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationRiskReport {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false) private String locationName;
    private String countryCode;
    @Column(nullable=false) private Double lat;
    @Column(nullable=false) private Double lon;
    @Column(nullable=false) private Integer overallRisk;
    @Column(nullable=false) private String riskLevel;
    @Column(columnDefinition="TEXT") private String summary;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb",nullable=false)
    private Map<String,Object> reportJson;
    @Column(nullable=false) private Instant generatedAt = Instant.now();
    private Instant expiresAt;
}
