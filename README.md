# Multi-Vendor eCommerce Marketplace

A study project: a multi-vendor marketplace where customers browse and buy products from many sellers, sellers manage their own store and orders, and admins moderate the platform.

**Stack:** Java 17+, Spring Boot 3/4, Spring Data JPA (Hibernate), MySQL, Spring Security 6 + OAuth2 Resource Server (JWT access token) + stateful refresh tokens, Stripe (PaymentIntents + webhooks), Lombok, Bean Validation. Frontend: React + TypeScript.

---

## 1. Architecture

Layered architecture, grouped by element type (not by feature):

```
com.ecommerce
├── EcommerceApplication.java
├── config            JwtConfig (encoder/decoder/converter), SecurityConfig, CorsConfig, 
│                     JpaConfig (auditing), WebConfig (/uploads), StripeConfig
├── jobs              Scheduled jobs: OrderExpirationJob, RefreshTokenCleanupJob
├── controllers       REST controllers (validation + delegation)
│   └── admin         admin controllers (api/admin/ endpoints)
├── services          Business logic, @Transactional
├── repositories      Spring Data JPA repositories
├── models
│   ├── entities      JPA entities (all extend AuditorEntity)
│   ├── enums         UserRole, UserStatus, SellerStatus, ProductStatus,
│   │                 OrderStatus, PaymentStatus, RefundStatus
│   └── DTO
│       ├── requests  Incoming payloads 
│       └── responses Outgoing payloads 
├── security          CustomUserDetails, CustomUserDetailsService, JwtService
├── utils             TokenUtils, OrderStatusCalculator
└── exceptions        domain exceptions
     └── handlers     ErrorCode, ErrorResponse, GlobalExceptionHandler
```

Request flow: `Controller → Service → Repository → MySQL`. Entities are never exposed; controllers return response DTOs.

### Key design decisions
- **Bidirectional `@OneToMany(mappedBy)` only where the parent owns the children** (Cart→CartItem, Order→SellerOrder→OrderItem, Product→ProductImage). Everything else is queried through repositories.
- **OAuth2 Resource Server + short-lived JWT access token.** Spring validates the `Bearer` token
 with a `JwtDecoder`.  Access tokens live 1 hour.
- **Stateful refresh tokens with rotation.** The refresh token is a random string stored in the
  `refresh_token` table. Every `/auth/refresh` revokes the used token and issues a new one.
- **Scheduled cleanup**: `RefreshTokenCleanupJob` deletes expired refresh and password reset tokens nightly.
- **Consistent error format** via `GlobalExceptionHandler`: `{ code, status, error, message, path, timestamp, fieldErrors }`.
- **Auditing** (`createdOn`, `updatedOn`) through `AuditorEntity` + `@EnableJpaAuditing`.
- **Money** is `BigDecimal`; Stripe amounts are converted to cents on the server.
- **Card data never touches the backend.** Only the Stripe PaymentIntent id is stored.

---

## 2. Domain flows

**Auth:** register (always as CUSTOMER) → login → `{ accessToken, refreshToken, tokenType, user }`.
When the access token expires (401), the client calls `POST /auth/refresh` with the refresh token
(no `Authorization` header) and receives a new pair. Logout revokes all refresh tokens of the user.
`/users/me` to update the profile.

**Seller application:** a user applies (`POST /sellers/apply`) → seller profile is `PENDING` → admin approves/rejects → on approval the user's role becomes `SELLER`. Sellers manage one store and their own products.

**Products:** created as `PENDING`, visible publicly only when `APPROVED` (admin moderation). Deleting is a **soft delete** (`DELETED`), so carts and orders referencing the product never break. Images are uploaded as files (JPEG/PNG/WebP, 5 MB) and served from `/uploads/**`. A user can `review` a product only after receiving it.

**Purchase flow:**
```
Cart  →  POST /checkout  →  Order (PENDING_PAYMENT) + SellerOrders + OrderItems (price/name snapshot)
      →  POST /payments/intent  →  Stripe PaymentIntent → clientSecret → Stripe.js on the frontend
      →  Stripe webhook (signature verified)  →  Order + SellerOrders = PAID
      →  Seller moves each SellerOrder: PROCESSING → SHIPPED → DELIVERED
```
- Stock is **reserved at checkout** with an atomic `UPDATE ... WHERE quantity >= qty`.
- Unpaid orders are cancelled by the customer or automatically after 30 minutes (`OrderExpirationJob`), and stock is restored.
- Deleting a product removes it from carts, but existing `OrderItem`s keep their snapshot.
- `Order.status` is derived from the slowest `SellerOrder`.
- Sellers can also buy, but not their own products.
- Customers can refund a paid order (or only one seller's part of it) 
- 
---

## 3. Database structure

All tables have `id` (PK, auto increment), `created_on`, `updated_on`. There is **no roles table**: the role is an enum column on `users`.

| Table                  | Main columns                                                                                                                             | Relations |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------|---|
| `users`                | email (unique), password (BCrypt), full_name, status (ACTIVE/SUSPENDED), role (CUSTOMER/SELLER/ADMIN)                                    | – |
| `addresses`            | full_name, phone, city, state, postal_code, country, default_address                                                                     | N:1 users |
| `seller_profiles`      | nipt, business_phone, business_address, status (PENDING/APPROVED/REJECTED/SUSPENDED), rejection_reason                                   | 1:1 users |
| `stores`               | name, description                                                                                                                        | 1:1 seller_profiles |
| `categories`           | name (unique)                                                                                                                            | – |
| `products`             | name, description, price, quantity (stock), status (PENDING/APPROVED/REJECTED/DELETED)                                                   | N:1 seller_profiles, N:1 categories |
| `product_images`       | url, primary_image                                                                                                                       | N:1 products |
| `carts`                | –                                                                                                                                        | 1:1 users |
| `cart_items`           | quantity                                                                                                                                 | N:1 carts, N:1 products |
| `orders`               | status, total_amount                                                                                                                     | N:1 users (customer), N:1 addresses (shipping) |
| `seller_orders`        | status, subtotal                                                                                                                         | N:1 orders, N:1 seller_profiles |
| `order_items`          | product_name, unit_price, quantity (snapshots)                                                                                           | N:1 seller_orders, N:1 products |
| `payments`             | stripe_payment_intent_id, amount, currency, status (PENDING, SUCCEEDED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED), failure_reason | N:1 orders |
| `reviews`              | rating, comment                                                                                                                          | *(entity exists, not implemented)* |
| `refresh_token`        | token (SHA-256 hash, unique), expiry_date, revoked                                                                                       | N:1 users |
| `password_reset_token` | token (SHA-256 hash, unique), expiry_date, used                                                                                          | N:1 users |
| `reviews`              | rating, comment (unique per user and product)                                                                                            | N:1 users, N:1 products |
| `refunds`              | amount, stripe_refund_id (unique), status (PENDING/SUCCEEDED/FAILED), failure_reason                                                     | N:1 payments, N:1 seller_orders (null = whole order) |

---

## 4. Main endpoints (prefix `/api`)

| Area | Endpoints                                                                                                                                          |
|---|----------------------------------------------------------------------------------------------------------------------------------------------------|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `POST /auth/forgot-password`, `POST /auth/reset-password`    |
| Users | `GET/PUT /users/me`                                                                                                                                |
| Sellers | `POST /sellers/apply`, `GET/PUT /sellers/me`, `/sellers/me/store`, `/sellers/me/products`, `/sellers/me/orders`                                    |
| Catalog | `GET /products` (search, filter, paging), `GET /products/{id}`, `GET /categories`, `GET /stores/{id}`                                              |
| Products | `POST/PUT/DELETE /products`, image upload (`multipart/form-data`, key `file`)                                                                      |
| Cart | `GET /cart`, `POST /cart/items`, `PUT/DELETE /cart/items/{id}`                                                                                     |
| Orders | `POST /checkout`, `GET /orders`, `GET /orders/{id}`, `POST /orders/{id}/cancel`                                                                    |
| Seller orders | `PATCH /seller-orders/{id}/status`                                                                                                                 |
| Payments | `POST /payments/intent`, `GET /payments/{id}`, `POST /payments/webhooks/stripe`                                                                    |
| Admin | `/admin/sellers`, `/admin/products`, `/admin/orders`, `/admin/reviews`, `/admin/refunds`, `/admin/orders/{id}/refunds`                                                         |
| Reviews | `GET /products/{id}/reviews`, `GET /products/{id}/reviews/summary`, `POST /products/{id}/reviews`, `PUT/DELETE /reviews/{id}`, `GET /reviews/mine` |
| Refunds | `POST /orders/{id}/refunds`, `GET /refunds/mine`                                                                                                   |

---

## 5. Not implemented yet

**Features**
- Notifications 

**Engineering**
- Unit and integration tests
---

## 6. Configuration

Environment variables (never commit them):

| Variable | Required | Purpose |
|---|---|---|
| `JWT_SECRET` | yes | HS256 signing key, Base64-encoded, at least 32 bytes after decoding |
| `STRIPE_SECRET_KEY` | yes | `sk_test_...` from the Stripe Dashboard |
| `STRIPE_WEBHOOK_SECRET` | yes | `whsec_...` from `stripe listen` |
| `MAIL_HOST` | no (default `localhost`) | SMTP host |
| `MAIL_PORT` | no (default `1025`) | SMTP port |
| `MAIL_USERNAME` | no | SMTP username |
| `MAIL_PASSWORD` | no | SMTP password (for Gmail, an app password) |
| `MAIL_SMTP_AUTH` | no (default `false`) | Enable SMTP authentication |
| `MAIL_STARTTLS` | no (default `false`) | Enable STARTTLS |

Other properties: `jwt.expiration-ms` (access token, 3600000 = 1 hour),
`jwt.refresh-expiration-ms` (refresh token, 604800000 = 7 days),
`app.frontend-url` (base URL used in password reset links),
`app.password-reset-expiration-minutes` (reset link validity, default 30),
`app.mail.from` (sender address), `app.upload-dir`, `app.currency`,
`app.order-expiration-minutes`, multipart limits, DB connection.

## 7. Running locally

1. Create the MySQL/MariaDB database and set the datasource properties.
2. Set the required environment variables (`JWT_SECRET`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`).
3. Start a local SMTP catcher on port `1025` (e.g. Mailpit) so password reset emails can be
   viewed without sending real emails. To use a real SMTP server, set the `MAIL_*` variables instead.
4. Run the app (`mvn spring-boot:run`).
5. For payments, forward Stripe webhooks:
   ```
   stripe listen --events "payment_intent.succeeded,payment_intent.payment_failed,refund.updated,refund.failed" --forward-to localhost:8081/api/payments/webhooks/stripe
   ```
6. Test cards: `4242 4242 4242 4242` (success), `4000 0000 0000 9995` (declined).