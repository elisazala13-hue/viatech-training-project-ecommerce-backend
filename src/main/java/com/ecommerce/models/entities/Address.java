package com.ecommerce.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "addresses")
public class Address extends AuditorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String phone;
    private String street;
    private String city;
    private String country;
    private String postalCode;
    private boolean defaultAddress;
}
