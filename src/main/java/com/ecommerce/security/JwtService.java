package com.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder refreshDecoder;

    @Value("${jwt.expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public JwtService(JwtEncoder encoder,
                      @Qualifier("refreshJwtDecoder") JwtDecoder refreshDecoder) {
        this.encoder = encoder;
        this.refreshDecoder = refreshDecoder;
    }

    public String generateAccessToken(UserDetails user) {
        return build(user.getUsername(), "access", accessExpirationMs);
    }

    public String generateRefreshToken(UserDetails user) {
        return build(user.getUsername(), "refresh", refreshExpirationMs);
    }

    public String extractUsernameFromRefresh(String token) {
        return refreshDecoder.decode(token).getSubject();
    }

    private String build(String subject, String type, long ttlMs) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusMillis(ttlMs))
                .id(UUID.randomUUID().toString())
                .claim("token_type", type)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
