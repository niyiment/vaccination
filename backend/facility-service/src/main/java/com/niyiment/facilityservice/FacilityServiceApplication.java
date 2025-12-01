package com.niyiment.facilityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@ComponentScan({"com.niyiment.facilityservice", "com.niyiment.facilityservice.mapper"})
public class FacilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FacilityServiceApplication.class, args);
    }

}
