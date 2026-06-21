package com.geosentinel.auth.repository;
import com.geosentinel.auth.model.*;
import org.springframework.data.jpa.repository.*;
import java.time.Instant; import java.util.Optional; import java.util.UUID;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    @Modifying @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now") void deleteAllExpired(Instant now);
}
