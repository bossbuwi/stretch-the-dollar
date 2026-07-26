package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    private JwtExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new JwtExceptionHandler(builder);
    }

    @Test
    void shouldHandleExpiredJwtException() {
        // Given
        ExpiredJwtException ex = new ExpiredJwtException(null, null, "Token expired");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(builder.build(request, ErrorCode.EXPIRED_TOKEN, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.EXPIRED_TOKEN, null, null, null);
    }

    @Test
    void shouldHandleMalformedJwtException() {
        // Given
        MalformedJwtException ex = new MalformedJwtException("Malformed token");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(request, ErrorCode.MALFORMED_JWT, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.MALFORMED_JWT, null, null, null);
    }

    @Test
    void shouldHandleSecurityException() {
        // Given
        SecurityException ex = new SecurityException("Invalid signature");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        when(builder.build(request, ErrorCode.INVALID_TOKEN_SIGNATURE, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INVALID_TOKEN_SIGNATURE, null, null, null);
    }

    @Test
    void shouldHandleGenericJwtExceptionAsUnauthenticated() {
        // Given
        // JwtException is abstract, so we create an anonymous subclass or use a mock.
        // Using a concrete subclass that is not one of the above (e.g., io.jsonwebtoken.UnsupportedJwtException)
        JwtException ex = new io.jsonwebtoken.UnsupportedJwtException("Unsupported JWT");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(request, ErrorCode.UNAUTHENTICATED, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.UNAUTHENTICATED, null, null, null);
    }

    @Test
    void shouldHandleNonJwtExceptionAsUnauthenticated() {
        // Given – This scenario shouldn't happen in production, but we test defensive behavior
        RuntimeException ex = new RuntimeException("Some other error");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(request, ErrorCode.UNAUTHENTICATED, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.UNAUTHENTICATED, null, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilder() {
        // Given
        ExpiredJwtException ex = new ExpiredJwtException(null, null, "Expired");
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("X-Token-Error", "expired")
                .build();

        when(builder.build(request, ErrorCode.EXPIRED_TOKEN, null, null, null))
                .thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Token-Error")).containsExactly("expired");
    }
}
