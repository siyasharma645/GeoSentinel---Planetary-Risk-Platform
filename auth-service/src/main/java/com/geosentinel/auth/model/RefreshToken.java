package com.geosentinel.auth.model;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="refresh_tokens") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(unique=true,nullable=false,length=512) private String token;
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false,updatable=false) private Instant createdAt = Instant.now();
}
