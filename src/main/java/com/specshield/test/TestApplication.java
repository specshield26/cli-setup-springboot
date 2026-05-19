package com.specshield.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot entry point. Optional for SpecShield testing — the
 * specs in this directory are what matter — but having a runnable app makes
 * the fixture look like a real service for the purposes of detection.
 *
 * Run with: mvn spring-boot:run
 * Then visit: http://localhost:8080/v3/api-docs (springdoc-generated spec)
 */
@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
