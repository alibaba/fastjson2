package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.time.DateTimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(DateTimeException.class, () -> jsonObject.getDate("time"));
    }

    @Test
    public void testDOM() {
        assertThrows(
                DateTimeException.class,
                () -> JSONObject.of("time", "2000-01-00 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeException.class,
                () -> JSONObject.of("time", "2000-02-30 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeException.class,
                () -> JSONObject.of("time", "2000-01-32 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeException.class,
                () -> JSONObject.of("time", "2000-03-32 00:00:00").getDate("time")
        );
        assertThrows(
                DateTimeException.class,
                () -> JSONObject.of("time", "2000-04-31 00:00:00").getDate("time")
        );
    }
}
