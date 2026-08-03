package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class ResourceNotFoundExceptionHandler extends BaseExceptionHandler<ResourceNotFoundException> {

    public ResourceNotFoundExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof ResourceNotFoundException rnfe) {
            log.error(rnfe.getMessage());
            return builder.build(request, rnfe.getErrorCode(), rnfe.getMessage(), null, null);
        }

        log.warn("ResourceNotFoundExceptionHandler received received incompatible exception: {}", ex.getClass().getSimpleName());
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }
}
