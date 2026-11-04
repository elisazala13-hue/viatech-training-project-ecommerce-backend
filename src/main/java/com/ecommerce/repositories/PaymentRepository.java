package com.ecommerce.repositories;

import com.ecommerce.models.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String intentId);
    List<Payment> findByOrderId(Long orderId);
}
