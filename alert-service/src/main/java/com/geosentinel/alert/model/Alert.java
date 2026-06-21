package com.geosentinel.alert.model;
import jakarta.persistence.*; import lombok.*;
import org.hibernate.annotations.JdbcTypeCode; import org.hibernate.type.SqlTypes;
import java.time.Instant; import java.util.Map; import java.util.UUID;
@Entity @Table(name="alerts") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alert {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AlertType alertType;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AlertLevel level;
    @Column(nullable=false) private String title;
    @Column(nullable=false,columnDefinition="TEXT") private String message;
    private String countryCode, region;
    private Double lat, lon;
    private UUID sourceId; private String sourceType;
    @Column(nullable=false) private String status="ACTIVE";
    @Column(nullable=false) private Instant createdAt=Instant.now();
    private Instant expiresAt;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") private Map<String,Object> metadata;
}
