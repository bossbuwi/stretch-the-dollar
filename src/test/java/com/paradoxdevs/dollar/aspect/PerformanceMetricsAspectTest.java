package com.paradoxdevs.dollar.aspect;

import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PerformanceMetricsAspectTest {

    @AfterEach
    void tearDown() {
        // nothing for now
    }

    public static class DummyAnnotated {
        @PerformanceMetrics
        public String annotated(String id) { return "ok"; }
    }

    @Test
    void log_recordsExecutionEnd_whenProceedSucceeds() throws Throwable {
        PerformanceMetricsAspect aspect = new PerformanceMetricsAspect();

        DummyAnnotated target = new DummyAnnotated();

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(DummyAnnotated.class.getMethod("annotated", String.class));
        when(signature.getDeclaringTypeName()).thenReturn(DummyAnnotated.class.getName());
        when(signature.getName()).thenReturn("annotated");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenReturn("ok");

        // attach list appender to capture logs
        Logger logger = (Logger) LoggerFactory.getLogger(PerformanceMetricsAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Object result = aspect.log(joinPoint);
        assertEquals("ok", result);

        // assert logs contain start and end messages
        boolean hasStart = listAppender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("Execution start"));
        boolean hasEnd = listAppender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("Execution end") && e.getFormattedMessage().contains("Finished in"));

        assertTrue(hasStart, "Expected start log message");
        assertTrue(hasEnd, "Expected end log message");

        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void log_logsExceptionAndRethrows_whenProceedThrows() throws Throwable {
        PerformanceMetricsAspect aspect = new PerformanceMetricsAspect();

        DummyAnnotated target = new DummyAnnotated();

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(DummyAnnotated.class.getMethod("annotated", String.class));
        when(signature.getDeclaringTypeName()).thenReturn(DummyAnnotated.class.getName());
        when(signature.getName()).thenReturn("annotated");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("boom"));

        // attach list appender to capture logs
        Logger logger = (Logger) LoggerFactory.getLogger(PerformanceMetricsAspect.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        assertThrows(RuntimeException.class, () -> aspect.log(joinPoint));

        boolean hasException = listAppender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("Exception in") && e.getFormattedMessage().contains("Time elapsed"));
        assertTrue(hasException, "Expected exception log message");

        logger.detachAppender(listAppender);
        listAppender.stop();
    }
}
