package com.ecommerce.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends AuditorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SellerOrder sellerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private BigDecimal unitPrice;
    private int quantity;
}