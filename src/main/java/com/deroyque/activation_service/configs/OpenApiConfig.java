package com.deroyque.activation_service.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI activationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Activation Service API")
                        .description("""
                                REST API for managing product activation requests.
                                Allows clients to create activation requests and
                                retrieve their current processing status.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Activation Service Team")));
    }
}