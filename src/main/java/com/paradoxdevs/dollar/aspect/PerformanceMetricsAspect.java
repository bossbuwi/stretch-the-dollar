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
@Order(1)
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
        String methodName = getMethodName(joinPoint);
        log.info("Execution start [{}]:", methodName);

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();

            long end = System.nanoTime();
            String executionTime = computePerformanceTime(start, end);

            log.info("Execution end [{}]: Finished in {}s", methodName, executionTime);

            return result;
        } catch (Throwable ex) {
            long end = System.nanoTime();
            String elapsedTime = computePerformanceTime(start, end);
            log.warn("Exception in [{}]: Time elapsed: {}s", methodName, elapsedTime, ex);
            throw ex;
        }
    }

    private static String getMethodName(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass()
                .getMethod(signature.getName(), signature.getParameterTypes());

        PerformanceMetrics annotation = method.getAnnotation(PerformanceMetrics.class);
        if (annotation == null) {
            // Fallback: check class annotation
            annotation = joinPoint.getTarget().getClass().getAnnotation(PerformanceMetrics.class);
        }

        return signature.getName();
    }

    private String computePerformanceTime(long start, long end) {
        long durationNs =  end - start;
        double durationSec = durationNs / 1_000_000_000.0;
        return String.format("%.3f", durationSec);
    }
}
