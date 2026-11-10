package com.ecommerce.services;


import com.ecommerce.exceptions.InvalidRefreshTokenException;
import com.ecommerce.models.entities.RefreshToken;
import com.ecommerce.models.entities.User;
import com.ecommerce.repositories.RefreshTokenRepository;
import com.ecommerce.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public record Issued(User user, String refreshToken) {}

    @Transactional
    public String issue(User user) {
        String raw = TokenUtils.randomToken();

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        // saves hashed in db always
        rt.setToken(TokenUtils.sha256(raw));
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);
        repo.save(rt);

        // sends raw token to frontend
        return raw;
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public Issued rotate(String raw) {
        RefreshToken old = repo.findByToken(TokenUtils.sha256(raw))
                .orElseThrow(InvalidRefreshTokenException::new);

        // in case of stolen token
        if (old.isRevoked()) {
            repo.revokeAllByUser(old.getUser());
            throw new InvalidRefreshTokenException();
        }
        if (old.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        old.setRevoked(true);
        User user = old.getUser();
        // initialize user because of lazy
        Hibernate.initialize(user);
        return new Issued(user, issue(user));
    }

    // when used or in logout
    @Transactional
    public void revoke(String raw) {
        repo.findByToken(TokenUtils.sha256(raw)).ifPresent(rt -> rt.setRevoked(true));
    }

    // in case of theft
    @Transactional
    public void revokeAll(User user) {
        repo.revokeAllByUser(user);
    }
}