package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONArrayTest4 {
    @Test
    public void test0() {
        JSONObject object = JSON.parseObject("{\"value\":[]}");
        JSONArray array = (JSONArray) object.get("value");
        assertEquals(0, array.size());
        assertEquals(array, array.clone());
    }

    @Test
    public void test1() {
        JSONArray array = new JSONArray().fluentAdd("2018-07-14 00:00:00");
        Date date = array.getSqlDate(0);
        assertNotNull(date);

        Timestamp ts = array.getTimestamp(0);
        assertNotNull(ts);
    }
}
