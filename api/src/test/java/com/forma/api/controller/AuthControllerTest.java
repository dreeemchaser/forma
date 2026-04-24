package com.forma.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forma.api.dto.AuthRequest;
import com.forma.api.dto.AuthResponse;
import com.forma.api.model.Role;
import com.forma.api.model.User;
import com.forma.api.service.AuthService;
import com.forma.api.utils.exceptions.BadCredentialsException;
import com.forma.api.utils.exceptions.GlobalExceptionHandler;
import com.forma.api.utils.exceptions.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("encodedPassword")
                .role(Role.REGULAR_USER)
                .build();
    }

    // --- LOGIN ---

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        AuthRequest request = new AuthRequest("testuser", "password123");
        when(authService.login(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        AuthRequest request = new AuthRequest("testuser", "wrongpass1");
        when(authService.login(any(AuthRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidRequestBody_returns400() throws Exception {
        AuthRequest request = new AuthRequest("ab", "pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- REGISTER ---

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        AuthRequest request = new AuthRequest("newuser", "password123");
        when(authService.register(any(AuthRequest.class))).thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_usernameTaken_returns409() throws Exception {
        AuthRequest request = new AuthRequest("existuser", "password123");
        when(authService.register(any(AuthRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("Username already taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_usernameTooShort_returns400() throws Exception {
        AuthRequest request = new AuthRequest("ab", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        AuthRequest request = new AuthRequest("validuser", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Note: /me endpoint relies on @AuthenticationPrincipal which requires
    // a full security context — covered by integration tests.
}
