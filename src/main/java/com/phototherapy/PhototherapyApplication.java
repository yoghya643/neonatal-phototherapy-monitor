package com.phototherapy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PhototherapyApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                PhototherapyApplication.class,
                args
        );
    }

    @Bean
    CommandLineRunner initializeSensorDatabase() {
        return args -> SensorDatabase.initializeDatabase();
    }
}
