package com.geosentinel.auth.dto;
import com.geosentinel.auth.model.Role; import lombok.*; import java.time.Instant; import java.util.UUID;
@Data @Builder public class AuthResponse {
    private String accessToken, refreshToken, tokenType;
    private long expiresIn;
    private UserInfo user;
    @Data @Builder public static class UserInfo {
        private UUID id; private String email, firstName, lastName; private Role role; private Instant createdAt;
    }
}
