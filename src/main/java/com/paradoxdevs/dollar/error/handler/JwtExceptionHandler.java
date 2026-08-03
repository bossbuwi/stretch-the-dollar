package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class JwtExceptionHandler extends BaseExceptionHandler<JwtException> {

    public JwtExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        if (ex instanceof ExpiredJwtException eje) {
            log.error(eje.getMessage());
            return builder.build(request, ErrorCode.EXPIRED_TOKEN, null, null, null);
        } else if (ex instanceof MalformedJwtException mwe) {
            log.error(mwe.getMessage());
            return builder.build(request, ErrorCode.MALFORMED_JWT, null, null, null);
        } else if (ex instanceof SecurityException se) {
            log.error(se.getMessage());
            return builder.build(request, ErrorCode.INVALID_TOKEN_SIGNATURE, null, null, null);
        }
        log.error(ex.getMessage(), ex);
        return builder.build(request, ErrorCode.UNAUTHENTICATED, null, null, null);
    }
}
