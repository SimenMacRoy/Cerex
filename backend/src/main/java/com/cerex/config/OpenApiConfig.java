package com.cerex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 configuration for Swagger UI and API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cerexOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Cerex — Global Culinary Platform API")
                .description("REST API for the Cerex culinary platform. "
                    + "Enables recipe discovery, meal ordering, cultural exploration, "
                    + "and AI-powered personalization.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Cerex Engineering Team")
                    .email("api@cerex.com")
                    .url("https://cerex.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://cerex.com/legal/api-terms")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development"),
                new Server().url("https://api.cerex.com").description("Production")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT access token obtained from /api/v1/auth/login")));
    }
}
