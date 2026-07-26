package com.paradoxdevs.dollar.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseExecutionAspectTest {

    public static class DummyRepo {
        public String findById(String id) { return "ok"; }
    }

    @Test
    void logEntityReturn_logsStartAndEnd_whenProceedSucceeds() throws Throwable {
        DatabaseExecutionAspect aspect = new DatabaseExecutionAspect();

        DummyRepo target = new DummyRepo();

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getName()).thenReturn("findById");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenReturn("ok");

        Logger logger = (Logger) LoggerFactory.getLogger(DatabaseExecutionAspect.class);
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        Object res = aspect.logEntityReturn(joinPoint);
        assertEquals("ok", res);

        boolean hasStart = appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("DATABASE ACCESS START"));
        boolean hasEnd = appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("DATABASE ACCESS END"));

        assertTrue(hasStart);
        assertTrue(hasEnd);

        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void logEntityReturn_logsErrorAndRethrows_whenProceedThrows() throws Throwable {
        DatabaseExecutionAspect aspect = new DatabaseExecutionAspect();

        DummyRepo target = new DummyRepo();

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getName()).thenReturn("findById");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("dbfail"));

        Logger logger = (Logger) LoggerFactory.getLogger(DatabaseExecutionAspect.class);
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        assertThrows(RuntimeException.class, () -> aspect.logEntityReturn(joinPoint));

        boolean hasError = appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("DATABASE ACCESS FAILED") || e.getLevel().toString().equals("ERROR"));
        assertTrue(hasError);

        logger.detachAppender(appender);
        appender.stop();
    }
}
