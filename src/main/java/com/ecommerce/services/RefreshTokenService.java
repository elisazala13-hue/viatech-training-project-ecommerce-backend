package com.ecommerce.services;


import com.ecommerce.exceptions.InvalidRefreshTokenException;
import com.ecommerce.models.entities.RefreshToken;
import com.ecommerce.models.entities.User;
import com.ecommerce.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repo;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public record Issued(User user, String refreshToken) {}

    @Transactional
    public String issue(User user) {
        return create(user);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Issued rotate(String raw) {
        RefreshToken old = repo.findByToken(hash(raw))
                .orElseThrow(this::unauthorized);

        // in case of stolen token
        if (old.isRevoked()) {
            repo.revokeAllByUser(old.getUser());
            throw unauthorized();
        }
        if (old.getExpiryDate().isBefore(Instant.now())) {
            throw unauthorized();
        }

        old.setRevoked(true);
        User user = old.getUser();

        // initialize user because of lazy
        Hibernate.initialize(user);
        return new Issued(user, create(user));
    }

    @Transactional
    public void revoke(String raw) {
        repo.findByToken(hash(raw)).ifPresent(rt -> rt.setRevoked(true));
    }

    @Transactional
    public void revokeAll(User user) {
        repo.revokeAllByUser(user);
    }

    private String create(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(hash(raw));
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);
        repo.save(rt);
        return raw;
    }

    private InvalidRefreshTokenException unauthorized() {
        return new InvalidRefreshTokenException();
    }

    private String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}