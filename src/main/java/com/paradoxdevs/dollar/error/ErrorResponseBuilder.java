package com.paradoxdevs.dollar.error;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ErrorResponseBuilder {

    public ResponseEntity<ErrorResponse> build(WebRequest request,
                                               ErrorCode code,
                                               String customMessage,
                                               List<String> inputFieldErrors,
                                               Object[] args) {

        String message = Optional.ofNullable(customMessage)
                .filter(msg -> !msg.isBlank())
                .orElseGet(() -> code.formatMessage(args));

        ArrayList<String> fieldErrors = Optional.ofNullable(inputFieldErrors)
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .filter(list -> !list.isEmpty())
                .orElse(null);

        ErrorResponse response = ErrorResponse.builder()
                .code(code.getCode())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(response, getStatus(code));
    }

    private HttpStatus getStatus(ErrorCode code) {
        return switch (code) {
            case UNAUTHENTICATED,
                 MALFORMED_JWT
                    -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED,
                 EXPIRED_TOKEN,
                 INVALID_TOKEN_SIGNATURE,
                 INVALID_USER
                    -> HttpStatus.FORBIDDEN;
            case REQUEST_VALIDATION_ERROR,
                 MALFORMED_REQUEST
                    -> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND
                    -> HttpStatus.NOT_FOUND;
            case RESOURCE_ALREADY_EXISTS
                    -> HttpStatus.UNPROCESSABLE_CONTENT;
            case FEATURE_DISABLED
                    -> HttpStatus.SERVICE_UNAVAILABLE;
            default
                    -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
