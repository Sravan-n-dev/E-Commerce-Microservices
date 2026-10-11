package com.example.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Simple JWT token provider using a generated secret key.
 * For production, externalize the secret and configure expiration.
 */
public class JwtTokenProvider {
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long validityInMs = 3600_000; // 1 hour

    public String createToken(String username, Collection<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);
        String authorities = roles.stream().collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String username = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        String auth = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("auth", String.class);
        Collection<? extends GrantedAuthority> authorities = auth == null || auth.isEmpty()
                ? java.util.Collections.emptyList()
                : java.util.Arrays.stream(auth.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        User principal = new User(username, "", authorities);
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
