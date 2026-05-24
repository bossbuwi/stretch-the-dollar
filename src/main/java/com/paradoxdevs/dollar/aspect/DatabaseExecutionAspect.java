package com.paradoxdevs.dollar.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DatabaseExecutionAspect {

    @Pointcut("@annotation(com.paradoxdevs.dollar.aspect.DatabaseExecution)")
    public void databaseCallingMethods() {}

    @Pointcut("execution(* com.paradoxdevs.dollar.repository..*(..))")
    public void repositoryMethods() {}

    @Around("databaseCallingMethods()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DatabaseExecution annotation = signature.getMethod().getAnnotation(DatabaseExecution.class);

        String description = annotation.value();
        String methodName = signature.getName();

        log.info("Execution start [{}]: {}", methodName, description);

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;
            log.info("Execution end [{}]: Finished in {}ms", methodName, executionTime);

            return result;
        } catch (Throwable ex) {
            log.warn("Exception in [{}]: {}", methodName, ex.getMessage());
            throw ex;
        }
    }

    @Around(("repositoryMethods()"))
    public Object logEntityReturn(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("DATABASE START: Executing query via {}", methodName);
        Object result = joinPoint.proceed();
        log.info("DATABASE END: {} returned: {}", methodName, result);

        return result;
    }
}
