package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(2)
public class PerformanceMetricsAspect {

    @Pointcut("@annotation(com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics)")
    public void methodWithAnnotation() {}

    @Pointcut("@within(com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics)")
    public void classWithAnnotation() {}

    @Pointcut("execution(public * *(..))")
    public void publicMethods() {}

    @Pointcut("methodWithAnnotation() || (classWithAnnotation() && publicMethods())")
    public void checkPerformanceMetrics() {}

    @Around("checkPerformanceMetrics()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PerformanceMetrics annotation = method.getAnnotation(PerformanceMetrics.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(PerformanceMetrics.class);
        }

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.info("Execution start [{}.{}]:", className,methodName);

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();

            long end = System.nanoTime();
            String executionTime = computePerformanceTime(start, end);

            log.info("Execution end [{}.{}]: Finished in {}s", className, methodName, executionTime);

            return result;
        } catch (Throwable ex) {
            long end = System.nanoTime();
            String elapsedTime = computePerformanceTime(start, end);
            log.warn("Exception in [{}.{}]: Time elapsed: {}s", className, methodName, elapsedTime, ex);
            throw ex;
        }
    }

    private String computePerformanceTime(long start, long end) {
        long durationNs =  end - start;
        double durationSec = durationNs / 1_000_000_000.0;
        return String.format("%.3f", durationSec);
    }
}
