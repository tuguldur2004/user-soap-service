package com.example.usersoap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.example.usersoap.service.AuthService;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:authdb3;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class AuthFlowIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void fullFlow_register_login_validate_authorize() {
        AuthService.RegisterResult reg = authService.registerUser("ivan", "ipass", "ivan@test.com");
        assertTrue(reg.success());

        AuthService.LoginResult login = authService.loginUser("ivan", "ipass");
        assertTrue(login.success());
        assertFalse(login.token().isBlank());

        AuthService.ValidateResult val = authService.validateToken(login.token());
        assertTrue(val.valid());
        assertEquals("ivan", val.username());

        AuthService.AuthorizeRoleResult userAllowed = authService.authorizeRole(login.token(), "USER");
        assertTrue(userAllowed.allowed());

        AuthService.AuthorizeRoleResult adminDenied = authService.authorizeRole(login.token(), "ADMIN");
        assertFalse(adminDenied.allowed());
    }
}
