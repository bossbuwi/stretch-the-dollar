package com.paradoxdevs.dollar.exception;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, WebRequest request) {
        if (e instanceof InternalAuthenticationServiceException iase) {
            log.error(iase.getMessage(), iase);
            return buildResponseEntity(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
        }
        return buildResponseEntity(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(ExpiredJwtException e, WebRequest request) {
        return buildResponseEntity(request, ErrorCode.EXPIRED_TOKEN, null, null, null);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(SignatureException e, WebRequest request) {
        return buildResponseEntity(request, ErrorCode.INVALID_TOKEN_SIGNATURE, null, null, null);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(MalformedJwtException e, WebRequest request) {
        return buildResponseEntity(request, ErrorCode.MALFORMED_JWT, null, null, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        log.warn(e.getMessage());
        return buildResponseEntity(request, e.getErrorCode(), e.getMessage(), null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestFields(MethodArgumentNotValidException e, WebRequest request) {
        ArrayList<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));
        return buildResponseEntity(request, ErrorCode.REQUEST_VALIDATION_ERROR, null, fieldErrors, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e, WebRequest request) {
        log.error(e.getMessage(), e);
        if (e.getCause() instanceof ConstraintViolationException cve) {
            // This only works for PostgreSQL
            String errorMessage = cve.getSQLException().getMessage();
            Pattern pattern = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\)");
            Matcher matcher = pattern.matcher(errorMessage);

            if (matcher.find()) {
                String affectedColumn = matcher.group(1);
                String duplicatedValue = matcher.group(2);
                Object[] args = new Object[] {affectedColumn, duplicatedValue};
                return buildResponseEntity(request, ErrorCode.RESOURCE_ALREADY_EXISTS, null, null, args);
            }
        }

        return buildResponseEntity(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
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

        return buildResponseEntity(request, errorCode, null, fieldErrors, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, WebRequest request) {
        log.error(e.getMessage(), e);
        return buildResponseEntity(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(WebRequest request,
                                                              ErrorCode code,
                                                              String customMessage,
                                                              List<String> fieldErrors,
                                                              Object[] args) {
        log.error(code.getErrorMessage());
        String finalMessage = Optional.ofNullable(customMessage).filter(msg -> !msg.isBlank()).orElseGet(() -> code.formatMessage(args));
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
            case UNAUTHORIZED, MALFORMED_JWT -> HttpStatus.UNAUTHORIZED;
            case EXPIRED_TOKEN, INVALID_TOKEN_SIGNATURE -> HttpStatus.FORBIDDEN;
            case REQUEST_VALIDATION_ERROR, MALFORMED_REQUEST-> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESOURCE_ALREADY_EXISTS -> HttpStatus.UNPROCESSABLE_CONTENT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
