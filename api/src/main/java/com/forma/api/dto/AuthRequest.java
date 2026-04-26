package com.forma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credentials for login or registration")
public record AuthRequest(

        @Schema(
                description = "Unique username — between 3 and 20 characters",
                example = "alice",
                minLength = 3,
                maxLength = 20
        )
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
        String username,

        @Schema(
                description = "Password — between 8 and 16 characters",
                example = "password123",
                minLength = 8,
                maxLength = 16
        )
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 16, message = "Password must be at least 8 characters - Max 16. ")
        String password

) {}
