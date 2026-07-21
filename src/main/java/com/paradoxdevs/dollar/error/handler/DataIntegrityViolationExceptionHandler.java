package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DataIntegrityViolationExceptionHandler extends BaseExceptionHandler<DataIntegrityViolationException> {

    public DataIntegrityViolationExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof DataIntegrityViolationException dae) {
            log.error(dae.getMessage(), dae);
            if (dae.getCause() instanceof ConstraintViolationException cve) {
                // This only works for PostgreSQL
                String errorMessage = cve.getSQLException().getMessage();
                Pattern pattern = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\)");
                Matcher matcher = pattern.matcher(errorMessage);

                if (matcher.find()) {
                    String affectedColumn = matcher.group(1);
                    String duplicatedValue = matcher.group(2);
                    Object[] args = new Object[] {affectedColumn, duplicatedValue};
                    return builder.build(request, ErrorCode.RESOURCE_ALREADY_EXISTS, null, null, args);
                }
            }
        }

        log.warn("DataAccessExceptionHandler received received incompatible exception: {}", ex.getClass().getSimpleName());
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }
}
