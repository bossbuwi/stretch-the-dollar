package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataIntegrityViolationExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    private DataIntegrityViolationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DataIntegrityViolationExceptionHandler(builder);
    }

    @Test
    void shouldExtractColumnAndDuplicatedValueFromPostgresException() {
        // Given
        String postgresError = "ERROR: duplicate key value violates unique constraint \"uk_email\"\n" +
                "  Detail: Key (email)=(john@test.com) already exists.";
        SQLException sqlEx = new SQLException(postgresError);
        ConstraintViolationException cve = new ConstraintViolationException("Constraint violation", sqlEx, "uk_email");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data integrity", cve);

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).build();

        // Capture the 'args' array passed to the builder
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);

        when(builder.build(
                eq(request),
                eq(ErrorCode.RESOURCE_ALREADY_EXISTS),
                eq(null),
                eq(null),
                argsCaptor.capture()
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        Object[] capturedArgs = argsCaptor.getValue();
        assertThat(capturedArgs).containsExactly("email", "john@test.com");
        // Verify that the builder was called with the correct parameters
        verify(builder).build(request, ErrorCode.RESOURCE_ALREADY_EXISTS, null, null, new Object[]{"email", "john@test.com"});
    }

    @Test
    void shouldFallbackToServerErrorWhenCauseIsNotConstraintViolation() {
        // Given
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Some other DB error", new RuntimeException("Nested"));

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
    void shouldFallbackToServerErrorWhenRegexDoesNotMatch() {
        // Given
        String weirdError = "Some weird constraint error without key pattern";
        SQLException sqlEx = new SQLException(weirdError);
        ConstraintViolationException cve = new ConstraintViolationException("", sqlEx, "");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("", cve);

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
    void shouldFallbackToServerErrorWhenSQLExceptionMessageIsNull() {
        // Given
        SQLException sqlEx = new SQLException((String) null);
        ConstraintViolationException cve = new ConstraintViolationException("", sqlEx, "");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("", cve);

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
    void shouldFallbackToServerErrorWhenGivenIncompatibleException() {
        // Given – handler receives an exception that is not DataIntegrityViolationException
        // (though factory would never route such, we test defensive logic)
        DataRetrievalFailureException ex = new DataRetrievalFailureException("Some other DB error");

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
        String postgresError = "Key (id)=(123) already exists.";
        SQLException sqlEx = new SQLException(postgresError);
        ConstraintViolationException cve = new ConstraintViolationException("", sqlEx, "");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("", cve);

        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .header("X-Error", "duplicate")
                .build();

        // The builder will return the custom response; we just capture args
        when(builder.build(any(), any(), any(), any(), any())).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Error")).containsExactly("duplicate");
    }
}
