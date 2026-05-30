package com.chatpress.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${chatpress.api.title:ChatPress API}")
    private String title;

    @Value("${chatpress.api.version:1.0}")
    private String version;

    @Bean
    public OpenAPI chatPressOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description("chatpress-v1 REST API — Markdown knowledge page publishing system"));
    }
}
