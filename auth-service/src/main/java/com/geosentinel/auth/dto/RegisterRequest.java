package com.geosentinel.auth.dto;
import com.geosentinel.auth.model.Role;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class RegisterRequest {
    @Email @NotBlank private String email;
    @NotBlank @Size(min=8) private String password;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private Role role = Role.CITIZEN;
}
