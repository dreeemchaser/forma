package com.forma.api.controller;

import com.forma.api.dto.AuthRequest;
import com.forma.api.dto.AuthResponse;
import com.forma.api.dto.UserResponse;
import com.forma.api.model.User;
import com.forma.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, and current-user endpoints. No token required for login or register.")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Login",
            description = "Authenticate with an existing username and password. Returns a signed JWT valid for 24 hours."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JWT returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — username or password failed constraints",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Username is required"))),
            @ApiResponse(responseCode = "401", description = "Bad credentials — username not found or password incorrect",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Bad credentials")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @Operation(
            summary = "Register",
            description = "Create a new account with a unique username. Returns a signed JWT valid for 24 hours. " +
                    "The new user is assigned the **REGULAR_USER** role by default."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created — JWT returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — username or password failed constraints",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Username must be between 3 and 20 characters."))),
            @ApiResponse(responseCode = "409", description = "Username already taken",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Username 'alice' is already taken")))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(authRequest));
    }

    @Operation(
            summary = "Get current user",
            description = "Returns the authenticated user's ID, username, and role. " +
                    "Use this to check who you are logged in as and whether you have moderator access.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", example = "Unauthorized")))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name()));
    }

}
