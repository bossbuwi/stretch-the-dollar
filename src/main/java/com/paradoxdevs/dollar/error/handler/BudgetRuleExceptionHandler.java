package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.exception.BudgetRuleException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class BudgetRuleExceptionHandler extends BaseExceptionHandler<BudgetRuleException> {
    public BudgetRuleExceptionHandler(ErrorResponseBuilder builder) {
        super(builder);
    }

    @Override
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        BudgetRuleException exception = (BudgetRuleException) ex;
        return builder.build(request, exception.getErrorCode(), exception.getMessage(), null, null);
    }
}
