package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest_error {
    @Test
    public void test_error() throws Exception {
        String text = "{\"value\":true}";

        Exception error = null;
        try {
            JSON.parseObject(text, Date.class);
        } catch (JSONException ex) {
            error = ex;
        }
        Assertions.assertNotNull(error);
    }

    @Test
    public void test_error_1() throws Exception {
        String text = "{1:true}";

        Exception error = null;
        try {
            JSON.parseObject(text, Date.class);
        } catch (JSONException ex) {
            error = ex;
        }
        Assertions.assertNotNull(error);
    }

    @Test
    public void test_error_2() throws Exception {
        String text = "{\"@type\":\"java.util.Date\",\"value\":true}";

        Exception error = null;
        try {
            JSON.parseObject(text, Date.class);
        } catch (JSONException ex) {
            error = ex;
        }
        Assertions.assertNotNull(error);
    }

    @Test
    public void test_error_3() throws Exception {
        String text = "{\"@type\":\"java.util.Date\",\"value\":true}";

        JSONObject object = JSON.parseObject(text);
        assertEquals("java.util.Date", object.get("@type"));
        assertEquals(true, object.get("value"));
    }

    @Test
    public void test_error_4() throws Exception {
        String text = "{\"@type\":\"java.util.Date\",1:true}";

        JSONObject object = JSON.parseObject(text);
        assertEquals("java.util.Date", object.get("@type"));
        assertEquals(true, object.get(1));
    }

    @Test
    public void test_error_5() throws Exception {
        String text = "\"xxxxxxxxx\"";

        Exception error = null;
        try {
            JSON.parseObject(text, Date.class);
        } catch (JSONException ex) {
            error = ex;
        }
        Assertions.assertNotNull(error);
    }
}
