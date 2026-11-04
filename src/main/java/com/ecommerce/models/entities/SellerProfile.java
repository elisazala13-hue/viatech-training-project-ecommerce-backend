package com.ecommerce.models.entities;

import com.ecommerce.models.enums.SellerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "seller_profiles")
public class SellerProfile extends AuditorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String nipt;
    private String businessPhone;
    private String businessAddress;

    @Enumerated(EnumType.STRING)
    private SellerStatus status = SellerStatus.PENDING;

    private String rejectionReason;
}