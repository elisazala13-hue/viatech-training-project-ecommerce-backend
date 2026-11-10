package com.ecommerce.repositories;

import com.ecommerce.models.entities.PasswordResetToken;
import com.ecommerce.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String tokenHash);

    @Modifying
    @Query("delete from PasswordResetToken t where t.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiryDate < :now")
    int deleteExpired(@Param("now") Instant now);
}
