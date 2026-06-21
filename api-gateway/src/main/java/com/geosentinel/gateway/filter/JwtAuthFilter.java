package com.geosentinel.gateway.filter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    @Value("${jwt.secret}") private String secret;
    public JwtAuthFilter() { super(Config.class); }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (auth == null || !auth.startsWith("Bearer "))
                return deny(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            try {
                Claims c = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build().parseSignedClaims(auth.substring(7)).getPayload();
                var req = exchange.getRequest().mutate()
                    .header("X-User-Id",    c.getSubject())
                    .header("X-User-Role",  c.get("role",  String.class))
                    .header("X-User-Email", c.get("email", String.class))
                    .build();
                return chain.filter(exchange.mutate().request(req).build());
            } catch (ExpiredJwtException e) { return deny(exchange, "Token expired", HttpStatus.UNAUTHORIZED); }
              catch (JwtException e)         { return deny(exchange, "Invalid token",  HttpStatus.UNAUTHORIZED); }
        };
    }
    private Mono<Void> deny(ServerWebExchange ex, String msg, HttpStatus status) {
        ServerHttpResponse res = ex.getResponse();
        res.setStatusCode(status);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] b = ("{\"error\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(b)));
    }
    public static class Config {}
}
