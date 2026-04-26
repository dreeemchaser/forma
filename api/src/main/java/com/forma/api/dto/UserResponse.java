package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Authenticated user's profile")
public record UserResponse(

        @Schema(description = "Unique user ID", example = "a1000000-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Username", example = "alice")
        String username,

        @Schema(description = "Role assigned to this user", example = "REGULAR_USER",
                allowableValues = {"REGULAR_USER", "MODERATOR"})
        String role

) {}
