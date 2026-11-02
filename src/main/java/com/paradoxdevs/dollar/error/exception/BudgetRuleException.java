package com.paradoxdevs.dollar.error.exception;

import com.paradoxdevs.dollar.error.ErrorCode;

public class BudgetRuleException extends ApiException {
    public BudgetRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
