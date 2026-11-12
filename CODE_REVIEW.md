# Multi-Vendor eCommerce Marketplace — Code Review

**Date:** 2026-11-11
**Project:** Viatech Training Project — eCommerce Backend
**Stack:** Java 17, Spring Boot 4.1.2, Spring Security 6 + OAuth2 Resource Server (JWT), Spring Data JPA (Hibernate), MySQL/MariaDB, Stripe, Lombok

---

## 1. Project Overview

A multi-vendor marketplace REST API where:
- **Customers** browse products, manage a cart, checkout, pay via Stripe, and review products.
- **Sellers** apply for approval, manage a store and products, and process orders.
- **Admins** moderate sellers, products, reviews, refunds, and users.

The project contains **148 Java files** across 6 packages: config (7), controllers (16), services (17), repositories (17), models/entities (13), models/enums (6), models/DTOs (38), security (3), utils (2), exceptions (7), jobs (2).

**Architecture:** Layered (Controller → Service → Repository → MySQL). Entities are never exposed directly; controllers return response DTOs. JWT-based stateless auth with stateful refresh tokens.

---

## 2. Architecture & Design Assessment

### Strengths
- Clean layered architecture with clear separation of concerns.
- DTOs properly isolate entities from the API boundary.
- Good use of pessimistic locking (`PESSIMISTIC_WRITE`) on checkout and order cancellation to prevent race conditions.
- Atomic stock reservation via `UPDATE ... WHERE quantity >= qty` prevents overselling.
- Refresh token rotation with revocation is a solid security pattern.
- Consistent error handling via `GlobalExceptionHandler` with a uniform `ErrorResponse` format.
- JPA auditing (`createdOn`, `updatedOn`) applied across all entities.
- Order status is correctly derived from the slowest seller order via `OrderStatusCalculator`.
- Product soft-delete (`DELETED` status) preserves referential integrity for existing orders.
- Price/name snapshots in `OrderItem` decouple historical orders from product changes.
- Stripe webhook signature verification is properly implemented.
- Idempotency key on Stripe refunds prevents duplicate refund charges.
- `@EntityGraph` usage in repositories avoids N+1 query problems on paginated lists.

### Weaknesses
- No unit or integration tests (only a placeholder `contextLoads()`).
- No OpenAPI/Swagger documentation.
- Notifications feature is declared but not implemented.
- Some entity relationships use `FetchType.EAGER` where `LAZY` would be safer.

---

## 3. Bugs & Critical Issues

### [x] BUG-1: `CorsConfig` Missing `@Configuration` Annotation
**Severity:** Critical
**File:** `config/CorsConfig.java` (line 10)

The class is missing the `@Configuration` annotation. Without it, Spring may not register the `CorsConfigurationSource` bean, causing CORS to fail entirely for the React frontend.

```java
// Current (broken):
public class CorsConfig {

// Should be:
@Configuration
public class CorsConfig {
```

### [x] BUG-2: `handleWebhook` Missing `@Transactional` — Payment Status May Not Persist
**Severity:** Critical
**File:** `services/PaymentService.java` (line 129)

The `handleWebhook()` method and its private helpers `onSucceeded()` and `onFailed()` are not annotated with `@Transactional`. While `onSucceeded()` calls `orderRepository.findByIdForUpdate()` (which opens a transaction via `@Lock`), the payment entity update in `onFailed()` may not be persisted if no other transactional method wraps it. If a `payment_intent.payment_failed` event arrives, the payment status change to `FAILED` could be silently lost.

```java
// Current:
@Transactional
public void handleWebhook(String payload, String signature) { ... }
```

### [x] BUG-3: `onSucceeded` — Payment Entity Not Explicitly Saved
**Severity:** High
**File:** `services/PaymentService.java` (line 150)

In `onSucceeded()`, the `payment` entity is loaded via `paymentRepository.findByStripePaymentIntentId()`, its status is changed to `SUCCEEDED`, but it is never explicitly saved. While dirty tracking in Hibernate should persist the change within the same transaction, this relies on implicit behavior. If the `@Transactional` annotation is missing (see BUG-2), the change is definitely lost.

### [x] BUG-4: `Category` Entity Missing `@Column(unique = true)`
**Severity:** High
**File:** `models/entities/Category.java` (line 14)

The README states `name (unique)` for the categories table, but the entity has no `@Column(unique = true)` constraint. This allows duplicate category names in the database.

```java
// Current:
private String name;

// Should be:
@Column(unique = true)
private String name;
```

### [x] BUG-5: `SellerProfile.user` Uses `FetchType.EAGER`
**Severity:** High
**File:** `models/entities/SellerProfile.java` (line 15)

```java
@OneToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "user_id", unique = true)
private User user;
```
can you explain why its eager and not lazy

### [x] BUG-6: `ProductResponse.from()` — Potential `NullPointerException` on `category` or `seller`
**Severity:** High
**File:** `models/DTO/responses/ProductResponse.java` (lines 34-35)

```java
p.getCategory().getId(), p.getCategory().getName(),
p.getSeller().getId(), images, p.getCreatedOn());
```

If a product's `category` or `seller` is null (e.g., a data inconsistency or a product in an intermediate state), this will throw a `NullPointerException`. Since both are `FetchType.LAZY`, accessing them without an `@EntityGraph` in some query paths could also throw `LazyInitializationException`.

### [x] BUG-7: `AuthService.logout()` — Only Revokes One Refresh Token
**Severity:** Medium
**File:** `services/AuthService.java` (line 78)

```java
public void logout(RefreshRequest req) {
    refreshTokenService.revoke(req.refreshToken());
}
```

The README says "Logout revokes all refresh tokens of the user." But the implementation only revokes the single token passed in the request. If a user is logged in on multiple devices, logging out from one device won't invalidate the tokens on other devices. This should call `refreshTokenService.revokeAll(user)` instead.

### [x] BUG-10: `OrderStatusCalculator.recalculate()` — Enum `naturalOrder()` May Not Reflect Business Progress
**Severity:** Medium
**File:** `utils/OrderStatusCalculator.java` (line 18)

```java
.min(Comparator.naturalOrder())
```

The code relies on the alphabetical/declaration order of `OrderStatus` enum values to determine the "slowest" seller order. The enum is:
```java
PENDING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
```

Alphabetically, `PAID < PENDING_PAYMENT < PROCESSING < REFUNDED < SHIPPED`. This means `PAID` would be considered "slower" than `PENDING_PAYMENT`, which is incorrect — `PENDING_PAYMENT` is the earliest state. The declaration order happens to work for the main flow, but `CANCELLED` (ordinal 5) and `REFUNDED` (ordinal 6) are filtered out, so the remaining ordinals (0-4) do match the business progression. However, this is fragile and should use an explicit ordinal mapping or a dedicated `getProgress()` method.


### [x] BUG-12: `handleWebhook` — Missing `refund.updated` and `refund.failed` Event Handling
**Severity:** Medium
**File:** `services/PaymentService.java` (lines 142-146)

The README and Stripe CLI setup mention forwarding `refund.updated` and `refund.failed` events:
```
stripe listen --events "payment_intent.succeeded,payment_intent.payment_failed,refund.updated,refund.failed"
```

But the webhook handler only processes `payment_intent.succeeded` and `payment_intent.payment_failed`. Refund events are silently ignored, meaning the backend never updates refund status from Stripe asynchronously.

### [x] BUG-14: `application.yml` — Hardcoded Database Credentials
**Severity:** Medium (Production)
**File:** `src/main/resources/application.yml` (lines 21-23)

```yaml
datasource:
  url: jdbc:mysql://localhost:3307/ecommerce
  username: root
  password: root
```

Database credentials are hardcoded. While the README says "never commit them," they are committed in the default config. These should use environment variables like `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`.

### BUG-15: `ReviewService.create()` — Product Not Fetched as APPROVED
**Severity:** Low
**File:** `services/ReviewService.java` (line 50)

```java
Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
```

Unlike `listForProduct()` which calls `getApprovedProduct()`, the `create()` method doesn't verify the product is `APPROVED`. A user could theoretically review a `PENDING` or `REJECTED` product if they somehow had a delivered order item for it.

---

## 5. Security Issues

### SEC-2: JWT Access Token Contains No User Role/Status Claims
**Severity:** Medium
**File:** `security/JwtService.java` (lines 23-27)

```java
JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(user.getUsername())
        .issuedAt(now)
        .expiresAt(now.plusMillis(accessExpirationMs))
        .build();
```

The JWT only contains the email as the subject. On every request, the `jwtAuthConverter` in `SecurityConfig` must load the user from the database to check their role and status. This means:
- A suspended user's existing JWT remains valid until expiration (1 hour) because the DB check happens on every request.
- Every authenticated request triggers a database query to load the user.

**Recommendation:** Add `role` and `status` claims to the JWT. On token creation, embed the role. On each request, the converter can check the claim first and only hit the DB if needed.


---

## 11. Recommendations (Prioritized)

1. **Fix BUG-1 immediately** — Add `@Configuration` to `CorsConfig`. Without it, the frontend cannot communicate with the backend.
2. **Fix BUG-2** — Add `@Transactional` to `handleWebhook()` in `PaymentService`.
3. **Fix BUG-7** — Change `AuthService.logout()` to revoke all refresh tokens for the user, not just the one passed in.
4. **Fix BUG-4** — Add `@Column(unique = true)` to `Category.name`.
5. **Fix BUG-5** — Change `SellerProfile.user` to `FetchType.LAZY` and add `@EntityGraph` where needed.
6. **Fix BUG-6** — Add null checks in `ProductResponse.from()` or ensure `@EntityGraph` always loads category and seller.
7. **Fix BUG-12** — Handle `refund.updated` and `refund.failed` Stripe webhook events.
10. **Externalize database credentials** via environment variables.
12. **Add JWT role/status claims** to reduce per-request DB queries.

---

## 12. Overall Assessment

This is a **well-structured, thoughtfully designed** training project. The architecture is clean, the domain model is well-normalized, and the critical paths (checkout, payment, refund) are implemented with proper concurrency controls (pessimistic locking, atomic stock updates). The JWT + refresh token pattern is correctly implemented with rotation and revocation.

The main gaps are:
- **A few critical bugs** — the CORS configuration and webhook transactionality will cause real failures.
- **Some security hardening** — JWT claims, and credential externalization.
- **Incomplete features** — notifications and webhook refund event handling.
