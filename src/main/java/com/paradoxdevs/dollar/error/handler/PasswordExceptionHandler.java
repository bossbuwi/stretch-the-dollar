package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.PasswordException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class PasswordExceptionHandler extends BaseExceptionHandler<PasswordException> {

    public PasswordExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof PasswordException pe) {
            log.error(pe.getMessage(), pe);
            return builder.build(request, pe.getErrorCode(), null, null, null);
        }

        log.warn("PasswordExceptionHandler received received incompatible exception: {}", ex.getClass().getSimpleName());
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }
}
