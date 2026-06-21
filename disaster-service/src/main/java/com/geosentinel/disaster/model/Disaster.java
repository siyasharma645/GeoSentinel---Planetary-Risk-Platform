package com.geosentinel.disaster.model;
import jakarta.persistence.*; import lombok.*;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.math.BigDecimal; import java.time.Instant; import java.util.Map; import java.util.UUID;
@Entity @Table(name="disasters") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Disaster {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(unique=true) private String externalId;
    @Column(nullable=false) private String type;
    private String subType,title,locationName,countryCode,source,sourceUrl;
    private BigDecimal lat,lon,severity,impactRadiusKm;
    @Column(nullable=false) @Enumerated(EnumType.STRING) private DisasterStatus status=DisasterStatus.ACTIVE;
    private Long populationAffected;
    private Instant startedAt,endedAt;
    @Column(nullable=false) private Instant lastUpdated=Instant.now();
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") private Map<String,Object> rawData;
    @PreUpdate public void onUpdate(){this.lastUpdated=Instant.now();}
}
