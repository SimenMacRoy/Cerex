package com.cerex.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration properties for Cerex custom settings.
 *
 * <p>Maps to {@code cerex.*} in application.yml.
 */
@Data
@Component
@ConfigurationProperties(prefix = "cerex")
public class CerexProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final Media media = new Media();

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpiration = 3600000;    // 1 hour
        private long refreshTokenExpiration = 604800000; // 7 days
        private String issuer = "cerex-platform";
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
    }

    @Data
    public static class Media {
        private String uploadDir = "./uploads";
        private long maxFileSize = 20971520; // 20MB
    }
}
