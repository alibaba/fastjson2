package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class KotlinUtilsWarningTest {
    static List<LogRecord> capture(Runnable action) {
        Logger logger = Logger.getLogger("com.alibaba.fastjson2.util.KotlinUtils");
        List<LogRecord> records = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        boolean useParentHandlers = logger.getUseParentHandlers();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        try {
            action.run();
        } finally {
            logger.removeHandler(handler);
            logger.setUseParentHandlers(useParentHandlers);
        }
        return records;
    }

    @Test
    public void warnsOncePerClassWhenParameterNamesUnresolved() {
        List<LogRecord> records = capture(() -> {
            assertTrue(KotlinUtils.warnParameterNamesUnresolved(WarnOnce.class, 1, 2, null));
            assertFalse(KotlinUtils.warnParameterNamesUnresolved(WarnOnce.class, 1, 2, null));
        });
        assertEquals(1, records.size());
        assertEquals(Level.WARNING, records.get(0).getLevel());
        String message = records.get(0).getMessage();
        assertTrue(message.contains(WarnOnce.class.getName()), message);
        assertTrue(message.contains("kotlin-reflect"), message);
    }

    @Test
    public void noWarningWhenParameterNamesResolved() {
        List<LogRecord> records = capture(() ->
                assertFalse(KotlinUtils.warnParameterNamesUnresolved(Resolved.class, 1, 2, new String[]{"a", "b"})));
        assertTrue(records.isEmpty());
    }

    @Test
    public void noWarningWhenConstructorHasNoParameters() {
        List<LogRecord> records = capture(() ->
                assertFalse(KotlinUtils.warnParameterNamesUnresolved(NoParams.class, 1, 0, null)));
        assertTrue(records.isEmpty());
    }

    @Test
    public void messageSuggestsDependencyWhenKotlinReflectAbsent() {
        List<LogRecord> records = capture(() ->
                assertTrue(KotlinUtils.warnParameterNamesUnresolved(StateAbsent.class, 1, 2, null)));
        String message = records.get(0).getMessage();
        assertTrue(message.contains("kotlin-reflect is not on the classpath"), message);
        assertTrue(message.contains("org.jetbrains.kotlin:kotlin-reflect"), message);
    }

    @Test
    public void messageReportsFailureWhenKotlinReflectPresent() {
        List<LogRecord> records = capture(() ->
                assertTrue(KotlinUtils.warnParameterNamesUnresolved(StatePresent.class, 2, 2, null)));
        String message = records.get(0).getMessage();
        assertTrue(message.contains("kotlin-reflect failed"), message);
        assertFalse(message.contains("Add org.jetbrains.kotlin:kotlin-reflect"), message);
    }

    private static class WarnOnce {
    }

    private static class Resolved {
    }

    private static class NoParams {
    }

    private static class StateAbsent {
    }

    private static class StatePresent {
    }
}
