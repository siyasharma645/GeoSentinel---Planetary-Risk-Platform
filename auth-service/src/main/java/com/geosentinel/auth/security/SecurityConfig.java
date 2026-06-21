package com.geosentinel.auth.security;
import com.geosentinel.auth.repository.UserRepository; import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*; import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final UserRepository repo;
    @Bean public SecurityFilterChain chain(HttpSecurity h) throws Exception {
        return h.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a
                .requestMatchers("/api/v1/auth/register","/api/v1/auth/login","/api/v1/auth/refresh",
                    "/api/v1/auth/health","/actuator/**","/swagger-ui/**","/api-docs/**").permitAll()
                .anyRequest().authenticated()).build();
    }
    @Bean public UserDetailsService uds() {
        return e -> repo.findByEmail(e).orElseThrow(()->new UsernameNotFoundException("User not found: "+e));
    }
    @Bean public AuthenticationProvider provider() {
        var p = new DaoAuthenticationProvider(); p.setUserDetailsService(uds()); p.setPasswordEncoder(encoder()); return p;
    }
    @Bean public AuthenticationManager mgr(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
    @Bean public PasswordEncoder encoder() { return new BCryptPasswordEncoder(12); }
}
