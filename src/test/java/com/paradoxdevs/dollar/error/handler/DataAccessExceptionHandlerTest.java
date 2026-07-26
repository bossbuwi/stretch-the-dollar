package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataAccessExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    private DataAccessExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DataAccessExceptionHandler(builder);
    }

    @Test
    void shouldReturnInternalServerErrorWithExceptionMessage() {
        // Given
        String errorMessage = "Connection pool exhausted";
        DataAccessException ex = new DataRetrievalFailureException(errorMessage);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.INTERNAL_SERVER_ERROR),
                eq(errorMessage),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, errorMessage, null, null);
    }

    @Test
    void shouldHandleNullMessageGracefully() {
        // Given
        DataAccessException ex = new DataRetrievalFailureException(null);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.INTERNAL_SERVER_ERROR),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilder() {
        // Given
        String errorMessage = "SQL error";
        DataAccessException ex = new DataRetrievalFailureException(errorMessage);
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Error", "DB-failure")
                .build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.INTERNAL_SERVER_ERROR),
                eq(errorMessage),
                eq(null),
                eq(null)
        )).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Error")).containsExactly("DB-failure");
    }

    @Test
    void shouldAlwaysUseInternalServerErrorRegardlessOfSubtype() {
        // Given
        // Use another concrete subclass: org.springframework.dao.DataAccessResourceFailureException
        DataAccessException ex = new org.springframework.dao.DataAccessResourceFailureException("Resource unavailable");
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.INTERNAL_SERVER_ERROR),
                eq("Resource unavailable"),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        handler.handleException(ex, request);

        // Then
        verify(builder).build(request, ErrorCode.INTERNAL_SERVER_ERROR, "Resource unavailable", null, null);
    }
}
