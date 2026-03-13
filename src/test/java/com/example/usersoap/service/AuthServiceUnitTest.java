package com.example.usersoap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.usersoap.model.AuthUser;
import com.example.usersoap.repository.AuthUserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private AuthUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService createServiceWithRealTokenService() {
        TokenService tokenService = new TokenService(
                "UnitTestSecretKeyUnitTestSecretKey123456",
                60_000L);
        return new AuthService(repository, passwordEncoder, tokenService);
    }

    @Test
    void registerUser_success_returnsUserRole() {
        AuthService authService = createServiceWithRealTokenService();

        when(repository.existsByUsername("bat")).thenReturn(false);
        when(repository.existsByEmail("bat@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed");

        AuthUser saved = new AuthUser();
        saved.setId(1);
        saved.setUsername("bat");
        saved.setEmail("bat@mail.com");
        saved.setRole("USER");

        when(repository.save(any(AuthUser.class))).thenReturn(saved);

        AuthService.RegisterResult result = authService.registerUser("bat", "1234", "bat@mail.com");

        assertTrue(result.success());
        assertEquals(1, result.userId());
        assertEquals("USER", result.role());
    }

    @Test
    void loginUser_success_generatesJwt() {
        AuthService authService = createServiceWithRealTokenService();

        AuthUser user = new AuthUser();
        user.setId(5);
        user.setUsername("admin");
        user.setEmail("a@mail.com");
        user.setEnabled(true);
        user.setRole("ADMIN");
        user.setPasswordHash("hash");

        when(repository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        AuthService.LoginResult result = authService.loginUser("admin", "secret");

        assertTrue(result.success());
        assertNotNull(result.token());
        assertFalse(result.token().isBlank());
    }

    @Test
    void validateToken_extractsRoleClaim() {
        AuthService authService = createServiceWithRealTokenService();

        TokenService tokenService = new TokenService(
                "UnitTestSecretKeyUnitTestSecretKey123456",
                60_000L);
        String token = tokenService.generateToken(9, "tsetseg", "USER");

        AuthService.ValidateResult result = authService.validateToken(token);

        assertTrue(result.valid());
        assertEquals(9, result.userId());
        assertEquals("tsetseg", result.username());
        assertEquals("USER", result.role());
    }
}
