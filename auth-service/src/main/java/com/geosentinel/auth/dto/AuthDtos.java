package com.geosentinel.auth.dto;

import com.geosentinel.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @Email @NotBlank
        private String email;
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private Role role = Role.CITIZEN;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data @lombok.Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;
    }

    @Data @lombok.Builder
    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private Instant createdAt;
    }

    @Data @lombok.Builder
    public static class ApiError {
        private int status;
        private String error;
        private String message;
        private Instant timestamp;
    }
}
