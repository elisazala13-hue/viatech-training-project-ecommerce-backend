package com.ecommerce.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "stores")
public class Store extends AuditorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", unique = true)
    private SellerProfile seller;

    @Column(unique = true)
    private String name;

    private String description;
}
