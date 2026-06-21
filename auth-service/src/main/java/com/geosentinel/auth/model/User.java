package com.geosentinel.auth.model;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant; import java.util.*; import java.util.UUID;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(unique=true,nullable=false) private String email;
    @Column(nullable=false) private String password;
    private String firstName, lastName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role = Role.CITIZEN;
    private boolean enabled = true;
    @Column(nullable=false,updatable=false) private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    @PreUpdate public void onUpdate() { this.updatedAt = Instant.now(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_"+role.name())); }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
