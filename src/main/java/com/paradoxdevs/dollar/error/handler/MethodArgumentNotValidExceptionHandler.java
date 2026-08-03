package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.paradoxdevs.dollar.error.ErrorCode.REQUEST_VALIDATION_ERROR;

@Slf4j
@Component
public class MethodArgumentNotValidExceptionHandler extends BaseExceptionHandler<MethodArgumentNotValidException> {

    public MethodArgumentNotValidExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            ArrayList<String> fieldErrors = manve.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toCollection(ArrayList::new));
            log.error(String.valueOf(fieldErrors));
            return builder.build(request, REQUEST_VALIDATION_ERROR, null, fieldErrors, null);
        }

        log.warn("MethodArgumentNotValidExceptionHandler received incompatible exception: {}", ex.getClass().getSimpleName());
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }
}
