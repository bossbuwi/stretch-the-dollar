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
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IllegalArgumentExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;
    @Mock
    private WebRequest request;
    private IllegalArgumentExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new IllegalArgumentExceptionHandler(builder);
    }

    @Test
    void shouldReturnValidationErrorWithExceptionMessage() {
        // Given
        String errorMessage = "Invalid parameter: 'id' must be positive";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.REQUEST_VALIDATION_ERROR),
                eq(errorMessage),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.REQUEST_VALIDATION_ERROR, errorMessage, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilder() {
        // Given
        String errorMessage = "Invalid input";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("X-Error-Code", "INVALID")
                .build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.REQUEST_VALIDATION_ERROR),
                eq(errorMessage),
                eq(null),
                eq(null)
        )).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Error-Code")).containsExactly("INVALID");
    }

    @Test
    void shouldHandleExceptionWithNullMessageGracefully() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException((String) null);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.REQUEST_VALIDATION_ERROR),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.REQUEST_VALIDATION_ERROR, null, null, null);
    }

    @Test
    void shouldAlwaysUseRequestValidationErrorCode() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Some message");

        when(builder.build(any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        // When
        handler.handleException(ex, request);

        // Then
        verify(builder).build(
                eq(request),
                eq(ErrorCode.REQUEST_VALIDATION_ERROR),  // Always this code
                eq("Some message"),
                eq(null),
                eq(null)
        );
    }
}
