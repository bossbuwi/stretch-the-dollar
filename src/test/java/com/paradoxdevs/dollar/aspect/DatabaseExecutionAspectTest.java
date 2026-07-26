package com.paradoxdevs.dollar.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseExecutionAspectTest {

    public static class DummyRepo {
        public String findById(String id) { return "ok"; }
    }

    @Test
    void logEntityReturn_logsStartAndEnd_whenProceedSucceeds() throws Throwable {
        DatabaseExecutionAspect aspect = new DatabaseExecutionAspect();

        DummyRepo target = new DummyRepo();

        java.lang.reflect.Method m = DummyRepo.class.getMethod("findById", String.class);
        TestDoubles.FakeMethodSignature signature = new TestDoubles.FakeMethodSignature(m);

        ProceedingJoinPoint joinPoint = TestDoubles.createProceedingJoinPointProxy(target, m, new Object[]{"1"}, "ok", null);

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

        java.lang.reflect.Method m2 = DummyRepo.class.getMethod("findById", String.class);
        ProceedingJoinPoint joinPoint = TestDoubles.createProceedingJoinPointProxy(target, m2, new Object[]{"1"}, null, new RuntimeException("dbfail"));

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
