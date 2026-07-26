package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
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
public class ResourceNotFoundExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;

    @Mock
    private WebRequest request;

    private ResourceNotFoundExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ResourceNotFoundExceptionHandler(builder);
    }

    @Test
    void shouldHandleResourceNotFoundExceptionWithErrorMessage() {
        // Given
        String expectedMessage = "User with id 123 not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(expectedMessage);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(builder.build(request, ex.getErrorCode(), expectedMessage, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ex.getErrorCode(), expectedMessage, null, null);
    }

    @Test
    void shouldPassMessageEvenIfNull() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        when(builder.build(request, ex.getErrorCode(), null, null, null))
                .thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ex.getErrorCode(), null, null, null);
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
    void shouldPropagateResponseEntityFromBuilderForResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header("X-Error", "missing")
                .build();

        when(builder.build(request, ex.getErrorCode(), "Not found", null, null))
                .thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Error")).containsExactly("missing");
    }
}
