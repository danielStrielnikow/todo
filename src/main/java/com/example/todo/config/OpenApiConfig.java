package com.example.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo API")
                        .description("REST API for tasks app")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Daniel Strielnikow")
                                .email("daniel.strielnikow@outlook.com")));
    }
}
