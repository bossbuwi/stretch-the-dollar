package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static com.paradoxdevs.dollar.error.ErrorCode.REQUEST_VALIDATION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MethodArgumentNotValidExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    @Mock
    private BindingResult bindingResult;

    private MethodArgumentNotValidExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MethodArgumentNotValidExceptionHandler(builder);
    }

    @Test
    void shouldExtractFieldErrorsAndReturnValidationError() {
        // Given
        FieldError error1 = new FieldError("user", "email", "must not be blank");
        FieldError error2 = new FieldError("user", "password", "must be at least 8 characters");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        ArgumentCaptor<List<String>> fieldErrorsCaptor = ArgumentCaptor.forClass(List.class);

        // ✅ All arguments use matchers (eq(...) or captor)
        when(builder.build(
                eq(request),
                eq(REQUEST_VALIDATION_ERROR),
                eq(null),
                fieldErrorsCaptor.capture(),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        List<String> capturedErrors = fieldErrorsCaptor.getValue();
        assertThat(capturedErrors).containsExactly(
                "email: must not be blank",
                "password: must be at least 8 characters"
        );
        verify(builder).build(request, REQUEST_VALIDATION_ERROR, null, capturedErrors, null);
    }

    @Test
    void shouldHandleEmptyFieldErrors() {
        // Given
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        ArgumentCaptor<List<String>> fieldErrorsCaptor = ArgumentCaptor.forClass(List.class);

        // ✅ All arguments use matchers
        when(builder.build(
                eq(request),
                eq(REQUEST_VALIDATION_ERROR),
                eq(null),
                fieldErrorsCaptor.capture(),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        assertThat(fieldErrorsCaptor.getValue()).isEmpty();
        verify(builder).build(request, REQUEST_VALIDATION_ERROR, null, List.of(), null);
    }

    @Test
    void shouldFallbackToInternalServerErrorForIncompatibleException() {
        // Given
        RuntimeException ex = new RuntimeException("Some other error");

        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        // ✅ No matchers needed because all arguments are raw values (consistency)
        when(builder.build(
                request,
                ErrorCode.INTERNAL_SERVER_ERROR,
                null,
                null,
                null
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
        FieldError error = new FieldError("user", "email", "invalid");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("X-Validation", "failed")
                .build();

        // ✅ All arguments use matchers
        when(builder.build(
                eq(request),
                eq(REQUEST_VALIDATION_ERROR),
                eq(null),
                eq(List.of("email: invalid")),   // We expect exactly this list
                eq(null)
        )).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Validation")).containsExactly("failed");
    }
}
