package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1252 {
    @Test
    public void testObject() {
        JSONObject object = new JSONObject();
        Date date = new Date();
        assertSame(date, object.getDate("date", date));
        object.put("date", date);
        assertSame(date, object.getDate("date", date));
    }

    @Test
    public void testArray() {
        JSONArray array = new JSONArray();
        array.add(null);
        Date date = new Date();
        assertSame(date, array.getDate(0, date));
        array.set(0, date);
        assertSame(date, array.getDate(0, date));
    }
}
