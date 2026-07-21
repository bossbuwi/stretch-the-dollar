package com.paradoxdevs.dollar.error.handler;

import com.paradoxdevs.dollar.error.ErrorResponseBuilder;
import com.paradoxdevs.dollar.error.strategy.ExceptionHandlingStrategy;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.ParameterizedType;

@RequiredArgsConstructor
public abstract class BaseExceptionHandler<T extends Exception> implements ExceptionHandlingStrategy {
    protected final ErrorResponseBuilder builder;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Exception> getSupportedException() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
}
