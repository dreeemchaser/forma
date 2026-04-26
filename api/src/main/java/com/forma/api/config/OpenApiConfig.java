package com.forma.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
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

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Forma API")
                        .description("""
                                ## Forma — Social Posting Platform

                                Forma is a minimalist social platform for sharing posts, likes, and comments. \
                                All content is automatically analysed by Claude AI for misinformation, toxicity, \
                                and spam before being published.

                                ### Authentication
                                Most write operations require a valid JWT token. Obtain one via `POST /api/auth/login` \
                                or `POST /api/auth/register`, then click **Authorize** and paste the token.

                                ### Roles
                                - **REGULAR_USER** — can read, post, like, and comment
                                - **MODERATOR** — all of the above, plus access to the moderation queue \
                                and the ability to confirm or dismiss AI flags

                                ### AI Moderation
                                Every post is scored by Claude on submission. Posts with a score above `0.6` are \
                                automatically flagged (`aiFlagged: true`) and surfaced in the moderation queue. \
                                A moderator can then confirm the flag (`flaggedMisleading: true`) or dismiss it.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Forma")
                                .url("https://github.com/forma"))
                        .license(new License()
                                .name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT token obtained from `POST /api/auth/login` or `POST /api/auth/register`. \
                                        Tokens expire after **24 hours**. \
                                        Paste the raw token — do not include the `Bearer ` prefix here.
                                        """)));
    }
}
