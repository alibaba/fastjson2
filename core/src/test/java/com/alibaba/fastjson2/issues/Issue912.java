package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class Issue912 {
    @Test
    public void testNull() {
        assertEquals(
                JSONObject.of("time", "0000-00-00").getDate("time"),
                JSONObject.of("time", "0000-00-00 00:00:00").getDate("time")
        );
    }

    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("time", "2000-00-00 00:00:00");
        assertThrows(DateTimeParseException.class, () -> jsonObject.getDate("time"));
    }

    @Test
    public void testDOM() {
        assertThrows(
                DateTimeParseException.class,
                () -> JSONObject.of("time", "2000-01-00 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeParseException.class,
                () -> JSONObject.of("time", "2000-02-30 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeParseException.class,
                () -> JSONObject.of("time", "2000-01-32 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeParseException.class,
                () -> JSONObject.of("time", "2000-03-32 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeParseException.class,
                () -> JSONObject.of("time", "2000-04-31 00:00:00").getDate("time")
        );
    }
}
