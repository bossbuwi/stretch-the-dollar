package com.paradoxdevs.dollar.exception;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.paradoxdevs.dollar.exception.ErrorCode.REQUEST_VALIDATION_ERROR;

@Slf4j
@Component
public class ErrorResponseFactory {

    public ResponseEntity<ErrorResponse> buildResponseEntity(AuthenticationException e, WebRequest request) {
        if (e instanceof InternalAuthenticationServiceException iase) {
            log.error(iase.getMessage(), iase);
            return processErrorData(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
        }
        return processErrorData(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(PasswordException e, WebRequest request) {
        return processErrorData(request, e.getErrorCode(), null, null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(AccessDeniedException e, WebRequest request) {
        return processErrorData(request, ErrorCode.INVALID_USER, null, null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(JwtException e, WebRequest request) {
        if (e instanceof ExpiredJwtException eje) {
            return processErrorData(request, ErrorCode.EXPIRED_TOKEN, null, null, null);
        } else if (e instanceof MalformedJwtException mwe) {
            return processErrorData(request, ErrorCode.MALFORMED_JWT, null, null, null);
        } else if (e instanceof SecurityException se) {
            return processErrorData(request, ErrorCode.INVALID_TOKEN_SIGNATURE, null, null, null);
        }
        return processErrorData(request, ErrorCode.UNAUTHENTICATED, null, null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(MethodArgumentNotValidException e, WebRequest request) {
        ArrayList<String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));
        return processErrorData(request, REQUEST_VALIDATION_ERROR, null, fieldErrors, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(IllegalArgumentException e, WebRequest request) {
        return processErrorData(request, ErrorCode.REQUEST_VALIDATION_ERROR, e.getMessage(), null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(ResourceNotFoundException e, WebRequest request) {
        return processErrorData(request, e.getErrorCode(), e.getMessage(), null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(FeatureDisabledException e, WebRequest request) {
        return processErrorData(request, ErrorCode.FEATURE_DISABLED, null, null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(DataAccessException e, WebRequest request) {
        return processErrorData(request, ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), null, null);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(DataIntegrityViolationException e, WebRequest request) {
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
                return processErrorData(request, ErrorCode.RESOURCE_ALREADY_EXISTS, null, null, args);
            }
        }
        return processErrorData(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    private ResponseEntity<ErrorResponse> processErrorData(WebRequest request,
                                                              ErrorCode code,
                                                              String customMessage,
                                                              List<String> fieldErrors,
                                                              Object[] args) {
        // TODO: Incorrect log. This does not incorporate the args.
        //  It results in the generic %s message on logs.
        //  Figure out the correct sequence.
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
            case UNAUTHENTICATED, MALFORMED_JWT -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED, EXPIRED_TOKEN,
                 INVALID_TOKEN_SIGNATURE, INVALID_USER -> HttpStatus.FORBIDDEN;
            case REQUEST_VALIDATION_ERROR, MALFORMED_REQUEST-> HttpStatus.BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESOURCE_ALREADY_EXISTS -> HttpStatus.UNPROCESSABLE_CONTENT;
            case FEATURE_DISABLED ->  HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
