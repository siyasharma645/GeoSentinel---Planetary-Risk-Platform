package com.geosentinel.auth.service;
import com.geosentinel.auth.dto.*; import com.geosentinel.auth.model.*;
import com.geosentinel.auth.repository.*; import com.geosentinel.auth.security.JwtService;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.UUID;
@Slf4j @Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo; private final RefreshTokenRepository rtRepo;
    private final JwtService jwt; private final PasswordEncoder enc; private final AuthenticationManager auth;
    @Value("${jwt.refresh-expiry-ms}") private long rtExp;
    @Transactional public AuthResponse register(RegisterRequest r) {
        if (userRepo.existsByEmail(r.getEmail())) throw new IllegalArgumentException("Email already registered");
        User u = userRepo.save(User.builder().email(r.getEmail()).password(enc.encode(r.getPassword()))
            .firstName(r.getFirstName()).lastName(r.getLastName()).role(r.getRole()).enabled(true).build());
        log.info("Registered {} as {}", u.getEmail(), u.getRole()); return resp(u);
    }
    @Transactional public AuthResponse login(LoginRequest r) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(r.getEmail(), r.getPassword()));
        User u = userRepo.findByEmail(r.getEmail()).orElseThrow();
        rtRepo.deleteByUser(u); return resp(u);
    }
    @Transactional public AuthResponse refresh(String token) {
        RefreshToken rt = rtRepo.findByToken(token).orElseThrow(()->new IllegalArgumentException("Invalid token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) { rtRepo.delete(rt); throw new IllegalArgumentException("Token expired"); }
        User u = rt.getUser(); rtRepo.delete(rt); return resp(u);
    }
    @Transactional public void logout(String uid) {
        userRepo.findById(UUID.fromString(uid)).ifPresent(rtRepo::deleteByUser);
    }
    private AuthResponse resp(User u) {
        String access = jwt.generate(u), refresh = UUID.randomUUID()+"-"+UUID.randomUUID();
        rtRepo.save(RefreshToken.builder().user(u).token(refresh).expiresAt(Instant.now().plusMillis(rtExp)).build());
        return AuthResponse.builder().accessToken(access).refreshToken(refresh)
            .tokenType("Bearer").expiresIn(jwt.getExp()/1000)
            .user(AuthResponse.UserInfo.builder().id(u.getId()).email(u.getEmail())
                .firstName(u.getFirstName()).lastName(u.getLastName())
                .role(u.getRole()).createdAt(u.getCreatedAt()).build()).build();
    }
    @Scheduled(cron="0 0 * * * *") @Transactional
    public void purgeExpired() { rtRepo.deleteAllExpired(Instant.now()); }
}
