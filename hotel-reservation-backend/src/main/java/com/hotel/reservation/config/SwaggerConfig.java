package com.hotel.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Hotel Reservation System API")
                .version("1.0.0")
                .description("REST API for Hotel Reservation Management System")
                .contact(new Contact()
                    .name("seba4s")
                    .email("seba4s@hotel.com")
                    .url("https://github.com/migueltovarb/ISWDISENO202502-1seba4s"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080" + contextPath)
                    .description("Development Server"),
                new Server()
                    .url("https://api.hotel-reservation.com" + contextPath)
                    .description("Production Server")))
            
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"))
            
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token without 'Bearer ' prefix")));
    }
}