package com.geosentinel.auth;
import com.geosentinel.auth.dto.*; import com.geosentinel.auth.model.*;
import com.geosentinel.auth.repository.*; import com.geosentinel.auth.security.JwtService;
import com.geosentinel.auth.service.AuthService;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) class AuthServiceTest {
    @Mock UserRepository uRepo; @Mock RefreshTokenRepository rtRepo;
    @Mock JwtService jwt; @Mock AuthenticationManager auth;
    @Spy BCryptPasswordEncoder enc = new BCryptPasswordEncoder(4);
    @InjectMocks AuthService svc;
    @Test @DisplayName("Register returns tokens") void regOk() {
        RegisterRequest r = new RegisterRequest(); r.setEmail("a@b.com"); r.setPassword("Pass@1234"); r.setFirstName("A"); r.setLastName("B"); r.setRole(Role.CITIZEN);
        when(uRepo.existsByEmail(any())).thenReturn(false);
        when(uRepo.save(any())).thenReturn(User.builder().email("a@b.com").firstName("A").lastName("B").role(Role.CITIZEN).build());
        when(jwt.generate(any())).thenReturn("tok"); when(jwt.getExp()).thenReturn(3600000L);
        when(rtRepo.save(any())).thenReturn(new RefreshToken());
        assertThat(svc.register(r).getAccessToken()).isEqualTo("tok");
    }
    @Test @DisplayName("Duplicate email throws") void dupEmail() {
        RegisterRequest r = new RegisterRequest(); r.setEmail("x@x.com");
        when(uRepo.existsByEmail("x@x.com")).thenReturn(true);
        assertThatThrownBy(()->svc.register(r)).isInstanceOf(IllegalArgumentException.class);
    }
}
