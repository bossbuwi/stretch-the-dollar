package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.request.PasswordRequest;
import com.paradoxdevs.dollar.api.response.AuthResponse;
import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private PasswordRequest passwordRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("testuser", "password123");
        passwordRequest = new PasswordRequest("testuser", "oldPassword", "newPassword", "newPassword");
        userResponse = new UserResponse("1", "testuser", "uuid-123", null);
        authResponse = new AuthResponse("testuser", "eyJhbGciOiJIUzI1NiJ9...");
    }

    @Test
    void testRegister_Success() {
        when(authService.register(any(AuthRequest.class))).thenReturn(userResponse);

        ResponseEntity<UserResponse> response = authController.register(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        verify(authService, times(1)).register(authRequest);
    }

    @Test
    void testLogin_Success() {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        verify(authService, times(1)).login(authRequest);
    }

    @Test
    void testChangePassword_Success() {
        doNothing().when(authService).changePassword(any(PasswordRequest.class));

        ResponseEntity<Void> response = authController.changePassword(passwordRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService, times(1)).changePassword(passwordRequest);
    }

    @Test
    void testRegister_CallsAuthServiceWithCorrectRequest() {
        authController.register(authRequest);

        verify(authService).register(authRequest);
    }

    @Test
    void testLogin_CallsAuthServiceWithCorrectRequest() {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        authController.login(authRequest);

        verify(authService).login(authRequest);
    }

    @Test
    void testChangePassword_CallsAuthServiceWithCorrectRequest() {
        authController.changePassword(passwordRequest);

        verify(authService).changePassword(passwordRequest);
    }
}
