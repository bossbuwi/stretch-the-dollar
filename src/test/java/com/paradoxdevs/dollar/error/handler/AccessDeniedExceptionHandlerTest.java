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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccessDeniedExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;
    @Mock
    private WebRequest request;
    private AccessDeniedExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AccessDeniedExceptionHandler(builder);
    }

    @Test
    void shouldReturnInvalidUserErrorForAccessDeniedException() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access is denied for this resource");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .code(ErrorCode.INVALID_USER.getCode())
                        .build());

        // Mock the builder to return the expected response when called with INVALID_USER
        when(builder.build(
                eq(request),
                eq(ErrorCode.INVALID_USER),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> actualResponse = handler.handleException(ex, request);

        // Then
        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INVALID_USER, null, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilder() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .header("X-Custom-Header", "custom-value")
                .build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.INVALID_USER),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Custom-Header")).containsExactly("custom-value");
    }

    @Test
    void shouldIgnoreExceptionMessageAndAlwaysReturnInvalidUser() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Some specific denial reason");

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        when(builder.build(any(WebRequest.class), any(ErrorCode.class), any(), any(), any()))
                .thenReturn(expectedResponse);

        // When
        handler.handleException(ex, request);

        // Then
        // Verify that it does NOT pass the exception's message to the builder
        verify(builder).build(
                eq(request),
                eq(ErrorCode.INVALID_USER),  // Always INVALID_USER, not a custom message
                eq(null),                    // customMessage is ALWAYS null
                eq(null),
                eq(null)
        );
    }

    @Test
    void shouldHandleExceptionWithNullMessageGracefully() {
        // Given
        AccessDeniedException ex = new AccessDeniedException(null);

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        when(builder.build(eq(request), eq(ErrorCode.INVALID_USER), eq(null), eq(null), eq(null)))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INVALID_USER, null, null, null);
    }
}
