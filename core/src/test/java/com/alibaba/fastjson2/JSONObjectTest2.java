package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONObjectTest2 {
    @Test
    public void testSqlDate() {
        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Date.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Timestamp.class));

        assertNotNull(JSONObject
                .of("date", "0000-00-00")
                .getObject("date", Time.class));
    }
}
