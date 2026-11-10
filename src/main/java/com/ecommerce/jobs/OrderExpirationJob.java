package com.ecommerce.jobs;

import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.repositories.OrderRepository;
import com.ecommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpirationJob {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.order-expiration-minutes:30}")
    private long expirationMinutes;

    // freq 5 minutes
    @Scheduled(fixedDelay = 300_000)
    public void cancelExpiredOrders() {
        // calculate createdOn for expired orders
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expirationMinutes);

        for (Long id : orderRepository.findIdsByStatusAndCreatedOnBefore(OrderStatus.PENDING_PAYMENT, cutoff)) {
            try {
                orderService.cancelExpired(id);
            } catch (Exception e) {
                log.warn("Could not cancel expired order {}", id, e);
            }
        }
    }
}
