package com.ecommerce.repositories;

import com.ecommerce.models.entities.Payment;
import com.ecommerce.models.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Payment> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, PaymentStatus status);

    List<Payment> findByOrderIdAndStatusIn(Long orderId, Collection<PaymentStatus> statuses);

    Optional<Payment> findByIdAndOrderCustomerId(Long id, Long customerId);

    Optional<Payment> findFirstByOrderIdAndStatusInOrderByIdDesc(Long orderId, Collection<PaymentStatus> statuses);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
