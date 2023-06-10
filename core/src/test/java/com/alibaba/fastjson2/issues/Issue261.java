package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue261 {
    @Test
    public void test0() {
        assertNull(JSON.parseObject("\"\"", Date.class));
        assertNull(JSON.parseObject("\"null\"", Date.class));
        assertNull(JSON.parseObject("null", Date.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Date.class)
        );

        assertNull(
                JSON.parseObject("\"\"", Calendar.class));
        assertNull(JSON.parseObject("\"null\"", Calendar.class));
        assertNull(JSON.parseObject("null", Calendar.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Calendar.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Date.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Date.class));
        assertNull(JSON.parseObject("null", java.sql.Date.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Date.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Timestamp.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Timestamp.class));
        assertNull(JSON.parseObject("null", java.sql.Timestamp.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Timestamp.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Time.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Time.class));
        assertNull(JSON.parseObject("null", java.sql.Time.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Time.class)
        );
    }
}
