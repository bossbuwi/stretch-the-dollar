package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.PasswordException;
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
public class PasswordExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;
    @Mock
    private WebRequest request;
    private PasswordExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PasswordExceptionHandler(builder);
    }

    @Test
    void shouldHandlePasswordExceptionWithItsErrorCode() {
        // Given
        ErrorCode expectedErrorCode = ErrorCode.UNAUTHORIZED; // or any other code
        PasswordException ex = new PasswordException(expectedErrorCode);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(request, expectedErrorCode, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, expectedErrorCode, null, null, null);
    }

    @Test
    void shouldHandlePasswordExceptionWithDefaultConstructor() {
        // Given
        PasswordException ex = new PasswordException(); // uses default ErrorCode.UNAUTHORIZED
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        when(builder.build(request, ErrorCode.UNAUTHORIZED, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }

    @Test
    void shouldFallbackToInternalServerErrorForIncompatibleException() {
        // Given
        RuntimeException ex = new RuntimeException("Something went wrong");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilderForPasswordException() {
        // Given
        PasswordException ex = new PasswordException(ErrorCode.UNAUTHORIZED);
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header("X-Auth", "failed")
                .build();

        when(builder.build(request, ErrorCode.UNAUTHORIZED, null, null, null))
                .thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Auth")).containsExactly("failed");
    }
}
