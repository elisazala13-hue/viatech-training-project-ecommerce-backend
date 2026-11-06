package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {}

// i dont know where you have used scheduling there is no need to create a class to just enable scheduling
//you can enable it in EcommerceApplication main page