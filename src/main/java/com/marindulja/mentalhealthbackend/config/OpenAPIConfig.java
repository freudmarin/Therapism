package com.marindulja.mentalhealthbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Mental Health Backend API")
                        .version("1.0")
                        .description("This API exposes endpoints to manage a mental health app.")
                        .contact(new Contact()
                                .name("Marin Dulja")
                                .email("duljamarin@gmail.com"))
                        .license(new License().name("MIT License").url("https://choosealicense.com/licenses/mit/")))
                .addServersItem(new Server().url("http://localhost:8080")
                        .description("Development server"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("MentalHealthBackend")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
