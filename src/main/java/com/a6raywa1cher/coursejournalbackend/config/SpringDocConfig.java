package com.a6raywa1cher.coursejournalbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
public class SpringDocConfig {
    @Value("${app.version}")
    private String version;

    @Value("${app.api-endpoint:#{null}}")
    private String apiEndpoint;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("course-journal-backend")
                        .version(version)
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/6rayWa1cher/course-journal-backend/blob/master/LICENSE")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes("basic",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
                        .addSecuritySchemes("jwt",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("jwt")
                        .addList("basic")
                );
        if (apiEndpoint != null) {
            openAPI = openAPI
                    .servers(List.of(new Server().url(apiEndpoint)));
        }
        return openAPI;
    }
}