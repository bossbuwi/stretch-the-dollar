package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.FeatureDisabledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureDisabledExceptionHandlerTest {
    @Mock
    private ErrorResponseBuilder builder;
    @Mock
    private WebRequest request;
    private FeatureDisabledExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FeatureDisabledExceptionHandler(builder);
    }

    @Test
    void shouldReturnFeatureDisabledErrorForFeatureDisabledException() {
        // Given
        FeatureDisabledException ex = new FeatureDisabledException();
        ResponseEntity<ErrorResponse> expectedResponse = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.FEATURE_DISABLED),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(expectedResponse);
        verify(builder).build(request, ErrorCode.FEATURE_DISABLED, null, null, null);
    }

    @Test
    void shouldPropagateResponseEntityFromBuilder() {
        // Given
        FeatureDisabledException ex = new FeatureDisabledException();
        ResponseEntity<ErrorResponse> customResponse = ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("X-Reason", "maintenance")
                .build();

        when(builder.build(
                eq(request),
                eq(ErrorCode.FEATURE_DISABLED),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(customResponse);

        // When
        ResponseEntity<ErrorResponse> result = handler.handleException(ex, request);

        // Then
        assertThat(result).isSameAs(customResponse);
        assertThat(result.getHeaders().get("X-Reason")).containsExactly("maintenance");
    }
}
