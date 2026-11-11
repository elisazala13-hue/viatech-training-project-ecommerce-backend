package com.ecommerce.repositories;

import com.ecommerce.models.entities.PaymentRefund;
import com.ecommerce.models.enums.RefundStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

@NullMarked
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r " +
            "WHERE r.payment.id = :paymentId AND r.status <> :excluded")
    BigDecimal sumExcluding(@Param("paymentId") Long paymentId,
                            @Param("excluded") RefundStatus excluded);

    Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

    @Override
    @EntityGraph(attributePaths = "payment")
    Page<PaymentRefund> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "payment")
    Page<PaymentRefund> findByPaymentOrderCustomerId(Long customerId, Pageable pageable);
}
