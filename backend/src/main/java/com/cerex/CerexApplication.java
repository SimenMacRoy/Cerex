package com.cerex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cerex Platform — Spring Boot Application Entry Point.
 *
 * <p>The monolith starter for Phase 1 MVP. Services are co-located in one
 * deployable JAR but organized by domain package. They will be extracted
 * into separate microservices in Phase 2 (EKS migration).
 *
 * @author Cerex Engineering Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
public class CerexApplication {

    public static void main(String[] args) {
        SpringApplication.run(CerexApplication.class, args);
    }
}
