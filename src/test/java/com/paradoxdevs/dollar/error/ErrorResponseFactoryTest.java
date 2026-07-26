package com.paradoxdevs.dollar.error;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import com.paradoxdevs.dollar.error.strategy.ExceptionHandlingStrategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ErrorResponseFactoryTest {

    @Test
    void buildResponseEntity_delegatesToExactStrategy() {
        ExceptionHandlingStrategy strategy = mock(ExceptionHandlingStrategy.class);
        doReturn((Class<? extends Exception>)(Class<?>) IllegalArgumentException.class).when(strategy).getSupportedException();

        ErrorResponse respBody = ErrorResponse.builder().code(400).message("bad").build();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody);
        when(strategy.handleException(any(), any())).thenReturn(expected);

        ErrorResponseBuilder builder = mock(ErrorResponseBuilder.class);
        ErrorResponseFactory factory = new ErrorResponseFactory(List.of(strategy), builder);

        WebRequest req = mock(WebRequest.class);
        ResponseEntity<ErrorResponse> actual = factory.buildResponseEntity(new IllegalArgumentException("x"), req);

        assertSame(expected, actual);
        verify(strategy, times(1)).handleException(any(IllegalArgumentException.class), eq(req));
        verifyNoInteractions(builder);
    }

    @Test
    void buildResponseEntity_usesSuperclassStrategy_whenExactMissing() {
        ExceptionHandlingStrategy strategy = mock(ExceptionHandlingStrategy.class);
        doReturn((Class<? extends Exception>)(Class<?>) RuntimeException.class).when(strategy).getSupportedException();

        ErrorResponse respBody = ErrorResponse.builder().code(409).message("conflict").build();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.status(HttpStatus.CONFLICT).body(respBody);
        when(strategy.handleException(any(), any())).thenReturn(expected);

        ErrorResponseBuilder builder = mock(ErrorResponseBuilder.class);
        ErrorResponseFactory factory = new ErrorResponseFactory(List.of(strategy), builder);

        WebRequest req = mock(WebRequest.class);
        ResponseEntity<ErrorResponse> actual = factory.buildResponseEntity(new IllegalArgumentException("oops"), req);

        assertSame(expected, actual);
        verify(strategy, times(1)).handleException(any(IllegalArgumentException.class), eq(req));
        verifyNoInteractions(builder);
    }

    @Test
    void buildResponseEntity_fallsBackToBuilder_whenNoStrategyFound() {
        ErrorResponseBuilder builder = mock(ErrorResponseBuilder.class);
        ErrorResponse fallbackBody = ErrorResponse.builder().code(500).message("fallback").build();
        ResponseEntity<ErrorResponse> fallback = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fallbackBody);
        when(builder.build(any(), any(), any(), any(), any())).thenReturn(fallback);

        ErrorResponseFactory factory = new ErrorResponseFactory(List.of(), builder);
        WebRequest req = mock(WebRequest.class);

        ResponseEntity<ErrorResponse> actual = factory.buildResponseEntity(new Exception("boom"), req);

        assertSame(fallback, actual);
        verify(builder, times(1)).build(eq(req), eq(ErrorCode.INTERNAL_SERVER_ERROR), anyString(), isNull(), isNull());
    }
}
