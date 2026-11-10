package com.ecommerce.jobs;

import com.ecommerce.repositories.PasswordResetTokenRepository;
import com.ecommerce.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository repo;
    private final PasswordResetTokenRepository resetRepo;


    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        int deleted = repo.deleteExpired(Instant.now());
        log.info("Deleted {} expired refresh tokens", deleted);
    }

    @Scheduled(cron = "0 5 3 * * *")
    @Transactional
    public void purgeExpiredResetTokens() {
        int deleted = resetRepo.deleteExpired(Instant.now());
        log.info("Deleted {} expired password reset tokens", deleted);
    }
}
