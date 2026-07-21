package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class IllegalArgumentExceptionHandler extends BaseExceptionHandler<IllegalArgumentException> {

    public IllegalArgumentExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        return builder.build(request, ErrorCode.REQUEST_VALIDATION_ERROR, ex.getMessage(), null, null);
    }
}