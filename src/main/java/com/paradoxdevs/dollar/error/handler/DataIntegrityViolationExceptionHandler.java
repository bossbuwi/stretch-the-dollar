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

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DataIntegrityViolationExceptionHandler extends BaseExceptionHandler<DataIntegrityViolationException> {

    private static final Pattern DUPLICATE_KEY_PATTERN = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\)");

    public DataIntegrityViolationExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        // Guard: wrong exception type
        if (!(ex instanceof DataIntegrityViolationException dae)) {
            log.warn("DataIntegrityViolationExceptionHandler received incompatible exception: {}", ex.getClass().getSimpleName());
            return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
        }

        // Log the original error
        log.error(dae.getMessage(), dae);

        // Try to extract duplicated key info
        Object[] args = extractDuplicatedKeyArgs(dae);
        if (args != null) {
            return builder.build(request, ErrorCode.RESOURCE_ALREADY_EXISTS, null, null, args);
        }

        // Fallback for any other case
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
    }

    /**
     * Extracts column and duplicated value from a PostgreSQL ConstraintViolationException.
     * Returns null if the info cannot be extracted.
     */
    private Object[] extractDuplicatedKeyArgs(DataIntegrityViolationException ex) {
        // Guard: cause must be ConstraintViolationException
        if (!(ex.getCause() instanceof ConstraintViolationException cve)) {
            return null;
        }

        // Guard: must have a SQLException
        SQLException sqlEx = cve.getSQLException();
        if (sqlEx == null) {
            return null;
        }

        // Guard: SQL message must not be null
        String errorMessage = sqlEx.getMessage();
        if (errorMessage == null) {
            return null;
        }

        // Try to match the PostgreSQL duplicate key pattern
        Matcher matcher = DUPLICATE_KEY_PATTERN.matcher(errorMessage);
        if (!matcher.find()) {
            return null;
        }

        // Extract and return column + value
        String affectedColumn = matcher.group(1);
        String duplicatedValue = matcher.group(2);
        return new Object[]{affectedColumn, duplicatedValue};
    }
}
