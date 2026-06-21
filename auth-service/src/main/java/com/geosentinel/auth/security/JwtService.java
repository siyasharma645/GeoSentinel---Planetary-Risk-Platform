package com.geosentinel.auth.security;
import com.geosentinel.auth.model.User; import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets;
import java.time.Instant; import java.util.*; import java.util.UUID;
@Service public class JwtService {
    private final SecretKey key; private final long exp;
    public JwtService(@Value("${jwt.secret}") String s, @Value("${jwt.expiry-ms}") long e) {
        key = Keys.hmacShaKeyFor(s.getBytes(StandardCharsets.UTF_8)); exp = e;
    }
    public String generate(User u) {
        Instant now = Instant.now();
        return Jwts.builder().subject(u.getId().toString())
            .claims(Map.of("email",u.getEmail(),"role",u.getRole().name(),
                           "firstName",u.getFirstName()!=null?u.getFirstName():""))
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(exp)))
            .signWith(key).compact();
    }
    public long getExp() { return exp; }
}
