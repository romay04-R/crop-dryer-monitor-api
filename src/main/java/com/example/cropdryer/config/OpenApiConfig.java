package com.example.cropdryer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${cropdryer.api.key:}")
    private String apiKey;

    @Bean
    public OpenAPI cropDryerOpenAPI() {
        final String securitySchemeName = "api-key";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Crop Dryer Monitor API")
                        .description("REST API for receiving and storing environmental data from Arduino-based crop drying systems")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Crop Dryer Monitor")
                                .email("support@cropdryer.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.cropdryer.example.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("API Key for authentication (optional, leave blank if not configured)")
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
