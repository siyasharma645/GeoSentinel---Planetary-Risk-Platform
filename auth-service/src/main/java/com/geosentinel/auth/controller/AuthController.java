package com.geosentinel.auth.controller;
import com.geosentinel.auth.dto.*; import com.geosentinel.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
@Tag(name="Auth",description="Authentication endpoints")
public class AuthController {
    private final AuthService svc;
    @PostMapping("/register") @Operation(summary="Register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.register(r)); }
    @PostMapping("/login")    @Operation(summary="Login")    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r) { return ResponseEntity.ok(svc.login(r)); }
    @PostMapping("/refresh")  @Operation(summary="Refresh")  public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String,String> b) { return ResponseEntity.ok(svc.refresh(b.get("refreshToken"))); }
    @PostMapping("/logout")   @Operation(summary="Logout")   public ResponseEntity<Map<String,String>> logout(@RequestHeader("X-User-Id") String uid) { svc.logout(uid); return ResponseEntity.ok(Map.of("message","Logged out")); }
    @GetMapping("/me")        @Operation(summary="Me")       public ResponseEntity<Map<String,String>> me(@RequestHeader("X-User-Id") String id, @RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Role") String role) { return ResponseEntity.ok(Map.of("id",id,"email",email,"role",role)); }
    @GetMapping("/health") public ResponseEntity<Map<String,String>> health() { return ResponseEntity.ok(Map.of("status","UP","service","auth-service")); }
}
