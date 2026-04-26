package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token returned on successful authentication")
public record AuthResponse(

        @Schema(
                description = "Signed JWT token. Valid for 24 hours. Pass as `Authorization: Bearer <token>` on subsequent requests.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9.abc123"
        )
        String token

) {}
