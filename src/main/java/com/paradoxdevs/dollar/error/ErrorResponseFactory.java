package com.paradoxdevs.dollar.error;

import com.paradoxdevs.dollar.api.response.ErrorResponse;
import com.paradoxdevs.dollar.error.strategy.ExceptionHandlingStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ErrorResponseFactory {
    private final Map<Class<? extends Exception>, ExceptionHandlingStrategy> strategyMap = new HashMap<>();
    private final Map<Class<? extends Exception>, ExceptionHandlingStrategy> resolvedCache = new ConcurrentHashMap<>();
    private final ErrorResponseBuilder builder;

    public ErrorResponseFactory(List<ExceptionHandlingStrategy> strategies, ErrorResponseBuilder builder) {
        this.builder = builder;
        for (ExceptionHandlingStrategy strategy : strategies) {
            strategyMap.put(strategy.getSupportedException(), strategy);
            log.info("Registered strategy for exception: {}", strategy.getSupportedException().getSimpleName());
        }
    }

    @PostConstruct
    public void init() {
        log.info("Total strategies registered: {}", strategyMap.size());
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception e, WebRequest request) {
        Class<? extends Exception> exceptionClass = e.getClass();

        // Check cache first
        ExceptionHandlingStrategy strategy = resolvedCache.get(exceptionClass);

        if (strategy == null) {
            // Traverse the class hierarchy
            Class<?> currentClass = exceptionClass;
            while (currentClass != null && !currentClass.equals(Object.class)) {
                // Check if we have a strategy for this specific class
                @SuppressWarnings("unchecked")
                Class<? extends Exception> castedClass = (Class<? extends Exception>) currentClass;
                strategy = strategyMap.get(castedClass);
                if (strategy != null) {
                    break;
                }
                // Move up to the superclass
                currentClass = currentClass.getSuperclass();
            }

            // Optional: Check interfaces if superclass traversal fails (for proxy classes or interface-based exceptions)
            if (strategy == null) {
                for (Class<?> interfaceClass : exceptionClass.getInterfaces()) {
                    if (Exception.class.isAssignableFrom(interfaceClass)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Exception> castedInterface = (Class<? extends Exception>) interfaceClass;
                        strategy = strategyMap.get(castedInterface);
                        if (strategy != null) {
                            break;
                        }
                    }
                }
            }

            // Cache the result to speed up future lookups (only cache non-null strategies)
            if (strategy != null) {
                resolvedCache.put(exceptionClass, strategy);
            }
        }

        if (strategy != null) {
            return strategy.handleException(e, request);
        }

        // Fallback for completely unregistered exceptions
        log.error("No specific strategy found for {}, using fallback", exceptionClass.getSimpleName(), e);
        return builder.build(request, ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected internal error", null, null);
    }
}
