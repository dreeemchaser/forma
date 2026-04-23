package com.forma.api.service;

import com.forma.api.dto.AuthRequest;
import com.forma.api.dto.AuthResponse;
import com.forma.api.model.Role;
import com.forma.api.model.User;
import com.forma.api.repository.UserRepository;
import com.forma.api.security.JwtService;
import com.forma.api.utils.exceptions.BadCredentialsException;
import com.forma.api.utils.exceptions.UserNotFoundException;
import com.forma.api.utils.exceptions.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRequest authRequest) {
        String username = authRequest.username();
        String password = authRequest.password();

        if(userRepository.existsByUsername(authRequest.username())){
            throw new UsernameAlreadyExistsException("Username already taken");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(Role.REGULAR_USER)
                .build();

        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(AuthRequest authRequest) {
        String username = authRequest.username();
        String password = authRequest.password();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username: " + username));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Bad credentials - Password Mismatch");
        }

        return new AuthResponse(jwtService.generateToken(user));
    }
}
