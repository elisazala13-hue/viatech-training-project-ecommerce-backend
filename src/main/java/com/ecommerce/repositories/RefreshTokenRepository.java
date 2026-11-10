package com.ecommerce.repositories;

import com.ecommerce.models.entities.RefreshToken;
import com.ecommerce.models.entities.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user = :user")
    void revokeAllByUser(@Param("user") User user);

    @Modifying
    @Query("delete from RefreshToken t where t.expiryDate < :now")
    int deleteExpired(@Param("now") Instant now);
}
