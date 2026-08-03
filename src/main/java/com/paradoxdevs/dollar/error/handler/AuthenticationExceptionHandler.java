package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Component
public class AuthenticationExceptionHandler extends BaseExceptionHandler<AuthenticationException> {

    public AuthenticationExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        AuthenticationException authEx = (AuthenticationException) ex;
        if (authEx instanceof InternalAuthenticationServiceException iase) {
            log.error(iase.getMessage(), iase);
            return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, null, null, null);
        }
        log.error(ex.getMessage(), ex);
        return builder.build(request, ErrorCode.UNAUTHORIZED, null, null, null);
    }
}
