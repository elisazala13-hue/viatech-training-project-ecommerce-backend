package com.ecommerce.repositories;

import com.ecommerce.models.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {}
