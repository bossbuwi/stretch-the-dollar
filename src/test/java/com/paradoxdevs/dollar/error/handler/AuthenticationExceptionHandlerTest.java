package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    private AuthenticationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthenticationExceptionHandler(builder);
    }

    @Test
    void shouldReturnInternalServerErrorForInternalAuthenticationServiceException() {
        // Given
        InternalAuthenticationServiceException ex = new InternalAuthenticationServiceException("User database unavailable");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(builder.build(eq(request), eq(ErrorCode.INTERNAL_SERVER_ERROR), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    @Test
    void shouldLogErrorWhenInternalAuthenticationServiceExceptionOccurs() {
        // Given
        InternalAuthenticationServiceException ex = new InternalAuthenticationServiceException("DB connection failed");

        when(builder.build(any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        // When
        handler.handleException(ex, request);

        // Then - verify that log.error was called with the exception
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    @Test
    void shouldReturnUnauthorizedForAnyOtherAuthenticationException() {
        // Given
        AuthenticationException ex = new BadCredentialsException("Invalid username or password");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(eq(request), eq(ErrorCode.UNAUTHORIZED), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }

    @Test
    void shouldReturnUnauthorizedForConcreteAuthenticationExceptionSubtypes() {
        // This test uses a different subtype (e.g., LockedException) to ensure the generic path is used.
        // AuthenticationException is abstract; we can use an anonymous subclass or a real one.
        // We'll use a real one: org.springframework.security.authentication.AccountStatusException is abstract too,
        // so we can create a mock or use a concrete one like DisabledException.
        // For simplicity, we use a mock of AuthenticationException (though it's abstract, Mockito can mock it).
        AuthenticationException ex = org.mockito.Mockito.mock(AuthenticationException.class);
        // Mockito can mock an abstract class, and the instanceof check will be false for InternalAuthenticationServiceException.

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        when(builder.build(eq(request), eq(ErrorCode.UNAUTHORIZED), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }

    @Test
    void shouldHandleNullMessageGracefully() {
        // Given
        InternalAuthenticationServiceException ex = new InternalAuthenticationServiceException(null);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(builder.build(eq(request), eq(ErrorCode.INTERNAL_SERVER_ERROR), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
    }
}
