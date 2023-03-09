package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.parser.JSONScanner;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1216 {
    @Test
    public void test() {
        String value = "2017-07-24 12:13:14";
        String value2 = "2018-07-24 12:13:14";

        Object[] objects = new Object[2];
        JSONScanner dateLexer = new JSONScanner(value);
        assertTrue(dateLexer.scanISO8601DateIfMatch());
        objects[0] = new Timestamp(dateLexer.getCalendar().getTimeInMillis());
        if (value2 != null) {
            dateLexer.close();
            dateLexer = new JSONScanner(value2);
            dateLexer.scanISO8601DateIfMatch(false);
            objects[1] = new Timestamp(dateLexer.getCalendar().getTimeInMillis());
        }
        dateLexer.close();
    }

    @Test
    public void test1() {
        JSONScanner dateLexer = new JSONScanner("xxx");
        assertFalse(dateLexer.scanISO8601DateIfMatch());
    }
}
