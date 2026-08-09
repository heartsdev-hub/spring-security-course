package com.spring_security.course_spring_security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String ISSUER = "course-spring-security";

    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private final Clock clock;

    public JwtService() {
        this.clock = Clock.systemUTC();
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now(clock);
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(now.plusMillis(jwtExpiration))
                )
                .signWith(getSigningKey())
                .compact();
    }


    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }


    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }


    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Object authorities = extractAllClaims(token).get("authorities");
        if (authorities instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }


    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(Date.from(Instant.now(clock)));
    }


    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
