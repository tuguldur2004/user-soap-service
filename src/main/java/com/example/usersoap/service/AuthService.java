package com.example.usersoap.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.usersoap.model.AuthUser;
import com.example.usersoap.repository.AuthUserRepository;

import io.jsonwebtoken.Claims;

@Service
@Transactional
public class AuthService {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(AuthUserRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public RegisterResult registerUser(String username, String rawPassword, String email) {
        if (repository.existsByUsername(username)) {
            return new RegisterResult(false, "Username already exists", 0, "");
        }
        if (repository.existsByEmail(email)) {
            return new RegisterResult(false, "Email already exists", 0, "");
        }

        AuthUser user = new AuthUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("USER");

        AuthUser saved = repository.save(user);
        return new RegisterResult(true, "Registered successfully", saved.getId(), saved.getRole());
    }

    public LoginResult loginUser(String username, String rawPassword) {
        AuthUser user = repository.findByUsername(username).orElse(null);
        if (user == null) {
            return new LoginResult(false, "Invalid credentials", "");
        }

        if (!user.isEnabled()) {
            return new LoginResult(false, "User is disabled", "");
        }

        boolean ok = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!ok) {
            return new LoginResult(false, "Invalid credentials", "");
        }

        String token = tokenService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResult(true, "Login successful", token);
    }

    public ValidateResult validateToken(String token) {
        try {
            Claims claims = tokenService.parse(token);
            Integer userId = claims.get("uid", Integer.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            return new ValidateResult(true, userId != null ? userId : 0,
                    username != null ? username : "", role != null ? role : "USER");
        } catch (Exception ex) {
            return new ValidateResult(false, 0, "", "");
        }
    }

    public AuthorizeRoleResult authorizeRole(String token, String requiredRole) {
        ValidateResult validation = validateToken(token);
        if (!validation.valid()) {
            return new AuthorizeRoleResult(false, "", "", "Invalid token");
        }

        String actualRole = validation.role() == null ? "" : validation.role();
        String expected = requiredRole == null ? "" : requiredRole.toUpperCase();
        boolean allowed = hasRole(actualRole, expected);

        return new AuthorizeRoleResult(
                allowed,
                validation.username(),
                actualRole,
                allowed ? "Access granted" : "Access denied for role " + actualRole);
    }

    private boolean hasRole(String actualRole, String requiredRole) {
        if (requiredRole == null || requiredRole.isBlank()) {
            return true;
        }
        String actual = actualRole == null ? "" : actualRole.toUpperCase();
        String required = requiredRole.toUpperCase();

        if ("ADMIN".equals(actual)) {
            return true;
        }
        return actual.equals(required);
    }

    public record RegisterResult(boolean success, String message, int userId, String role) {
    }

    public record LoginResult(boolean success, String message, String token) {
    }

    public record ValidateResult(boolean valid, int userId, String username, String role) {
    }

    public record AuthorizeRoleResult(boolean allowed, String username, String role, String message) {
    }
}
