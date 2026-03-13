package com.example.usersoap.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.usersoap.service.AuthService;

import com.example.usersoap.wsdl.AuthorizeRoleRequest;
import com.example.usersoap.wsdl.AuthorizeRoleResponse;
import com.example.usersoap.wsdl.LoginUserRequest;
import com.example.usersoap.wsdl.LoginUserResponse;
import com.example.usersoap.wsdl.RegisterUserRequest;
import com.example.usersoap.wsdl.RegisterUserResponse;
import com.example.usersoap.wsdl.ValidateTokenRequest;
import com.example.usersoap.wsdl.ValidateTokenResponse;

@Endpoint
public class UserAuthEndpoint {

    private static final String NAMESPACE_URI = "http://userauth.soap.service/";

    private final AuthService authService;

    public UserAuthEndpoint(AuthService authService) {
        this.authService = authService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "RegisterUserRequest")
    @ResponsePayload
    public RegisterUserResponse registerUser(@RequestPayload RegisterUserRequest request) {
        AuthService.RegisterResult result = authService.registerUser(request.getUsername(), request.getPassword(),
                request.getEmail());

        RegisterUserResponse response = new RegisterUserResponse();
        response.setSuccess(result.success());
        response.setMessage(result.message());
        response.setUserId(result.userId());
        response.setRole(result.role());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "LoginUserRequest")
    @ResponsePayload
    public LoginUserResponse loginUser(@RequestPayload LoginUserRequest request) {
        AuthService.LoginResult result = authService.loginUser(request.getUsername(), request.getPassword());

        LoginUserResponse response = new LoginUserResponse();
        response.setSuccess(result.success());
        response.setMessage(result.message());
        response.setToken(result.token());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ValidateTokenRequest")
    @ResponsePayload
    public ValidateTokenResponse validateToken(@RequestPayload ValidateTokenRequest request) {
        AuthService.ValidateResult result = authService.validateToken(request.getToken());

        ValidateTokenResponse response = new ValidateTokenResponse();
        response.setValid(result.valid());
        response.setUserId(result.userId());
        response.setUsername(result.username());
        response.setRole(result.role());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AuthorizeRoleRequest")
    @ResponsePayload
    public AuthorizeRoleResponse authorizeRole(@RequestPayload AuthorizeRoleRequest request) {
        AuthService.AuthorizeRoleResult result = authService.authorizeRole(request.getToken(),
                request.getRequiredRole());

        AuthorizeRoleResponse response = new AuthorizeRoleResponse();
        response.setAllowed(result.allowed());
        response.setUsername(result.username());
        response.setRole(result.role());
        response.setMessage(result.message());
        return response;
    }
}
