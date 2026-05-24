package com.paradoxdevs.dollar.exception;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        log.warn(e.getMessage());
        return buildResponseEntity(request, e.getErrorCode(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestFields(MethodArgumentNotValidException e, WebRequest request) {
        ArrayList<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));
        return buildResponseEntity(request, ErrorCode.REQUEST_VALIDATION_ERROR, null, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJsonInput(HttpMessageNotReadableException e, WebRequest request) {
        Throwable cause = e.getCause();

        ErrorCode errorCode = ErrorCode.REQUEST_VALIDATION_ERROR;
        List<String> fieldErrors = new ArrayList<>();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            String type = ife.getTargetType().getSimpleName();
            String fieldError = String.format("%s: Invalid value, expected type: %s", fieldName, type);
            fieldErrors.add(fieldError);
        } else if (cause instanceof StreamReadException sr) {
            fieldErrors = null;
            errorCode = ErrorCode.MALFORMED_REQUEST;
        }

        return buildResponseEntity(request, errorCode, null, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, WebRequest request) {
        log.error(e.getMessage(), e);
        return buildResponseEntity(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(WebRequest request, ErrorCode code, String message, List<String> fieldErrors) {
        String finalMessage = Optional.ofNullable(message).filter(msg -> !msg.isBlank()).orElse(code.getErrorMessage());
        ArrayList<String> finalFieldErrors = Optional.ofNullable(fieldErrors)
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(ArrayList::new)))
                .filter(list -> !list.isEmpty())
                .orElse(null);
        ErrorResponse response = ErrorResponse.builder()
                .code(code.getCode())
                .message(finalMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .fieldErrors(finalFieldErrors)
                .build();
        return new ResponseEntity<>(response, getStatus(code));
    }

    private HttpStatus getStatus(ErrorCode code) {
        return switch (code) {
            case REQUEST_VALIDATION_ERROR, MALFORMED_REQUEST-> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
