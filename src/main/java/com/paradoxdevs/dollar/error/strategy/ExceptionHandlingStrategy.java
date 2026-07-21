package com.paradoxdevs.dollar.error.strategy;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public interface ExceptionHandlingStrategy {
    ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request);
    Class<? extends Exception> getSupportedException();
}
