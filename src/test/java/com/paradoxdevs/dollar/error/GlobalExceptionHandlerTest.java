package com.paradoxdevs.dollar.error;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    @Test
    void handleException_delegatesToFactory_andReturnsResponse() {
        ErrorResponseFactory factory = mock(ErrorResponseFactory.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(factory);

        WebRequest req = mock(WebRequest.class);
        Exception ex = new RuntimeException("boom");

        ErrorResponse body = ErrorResponse.builder().code(500).message("err").build();
        ResponseEntity<ErrorResponse> expected = ResponseEntity.status(500).body(body);
        when(factory.buildResponseEntity(eq(ex), eq(req))).thenReturn(expected);

        ResponseEntity<ErrorResponse> actual = handler.handleException(ex, req);

        assertSame(expected, actual);
        verify(factory, times(1)).buildResponseEntity(eq(ex), eq(req));
    }
}
