package com.forma.api.controller;

import com.forma.api.dto.AuthRequest;
import com.forma.api.dto.AuthResponse;
import com.forma.api.dto.UserResponse;
import com.forma.api.model.User;
import com.forma.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(authRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name()));
    }

}
