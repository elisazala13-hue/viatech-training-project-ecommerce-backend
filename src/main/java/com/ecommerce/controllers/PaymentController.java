package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.CreatePaymentIntentRequest;
import com.ecommerce.models.DTO.responses.PaymentIntentResponse;
import com.ecommerce.models.DTO.responses.PaymentResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentIntentResponse createIntent(@AuthenticationPrincipal CustomUserDetails principal,
                                              @Valid @RequestBody CreatePaymentIntentRequest request) {
        return paymentService.createIntent(principal.getUser().getId(), request.orderId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse getById(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id) {
        return paymentService.getMine(principal.getUser().getId(), id);
    }

    @PostMapping("/webhooks/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
