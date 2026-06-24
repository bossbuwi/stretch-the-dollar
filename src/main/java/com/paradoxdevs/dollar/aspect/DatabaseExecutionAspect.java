package com.paradoxdevs.dollar.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DatabaseExecutionAspect {

    @Pointcut("execution(* com.paradoxdevs.dollar.repository..*(..))")
    public void repositoryMethods() {}

    @Around("repositoryMethods()")
    public Object logEntityReturn(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.debug("DATABASE ACCESS START: Executing query via {}", methodName);

        try {
            Object result = joinPoint.proceed();
            log.debug("DATABASE ACCESS END: {}", methodName);
            return result;
        } catch (Throwable t) {
            log.error("DATABASE ACCESS FAILED: {} - {}", methodName, t.getMessage(), t);
            throw t;
        }
    }
}
