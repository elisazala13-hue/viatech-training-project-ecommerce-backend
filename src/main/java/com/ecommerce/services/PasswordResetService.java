package com.ecommerce.services;

import com.ecommerce.models.entities.PasswordResetToken;
import com.ecommerce.models.entities.User;
import com.ecommerce.models.enums.UserStatus;
import com.ecommerce.repositories.PasswordResetTokenRepository;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository repo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Value("${app.password-reset-expiration-minutes}")
    private long expirationMinutes;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void requestReset(String emailInput) {
        String email = emailInput.trim().toLowerCase();

        userRepository.findByEmail(email)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .ifPresent(user -> {
                    repo.deleteByUser(user);

                    String raw = TokenUtils.randomToken();
                    PasswordResetToken t = new PasswordResetToken();
                    t.setUser(user);
                    t.setToken(TokenUtils.sha256(raw));
                    t.setExpiryDate(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
                    repo.save(t);

                    emailService.sendPasswordReset(
                            user.getEmail(), frontendUrl + "/reset-password?token=" + raw);
                });
    }

    @Transactional
    public void reset(String raw, String newPassword) {
        PasswordResetToken t = repo.findByToken(TokenUtils.sha256(raw))
                .orElseThrow(this::invalid);

        if (t.isUsed() || t.getExpiryDate().isBefore(Instant.now())) {
            throw invalid();
        }
        User user = t.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw invalid();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        t.setUsed(true);
        refreshTokenService.revokeAll(user);
    }

    private ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token");
    }
}