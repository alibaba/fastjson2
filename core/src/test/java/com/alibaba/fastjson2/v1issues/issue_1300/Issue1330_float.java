package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by wenshao on 30/07/2017.
 */
public class Issue1330_float {
    @Test
    public void test() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class));
    }

    @Test
    public void test_array() throws Exception {
        String str = "{\"value\":[]}";
        assertEquals(0F, JSON.parseObject(str, Model.class).value);
        assertEquals(0F, JSON.parseObject(str.toCharArray(), Model.class).value);
        assertEquals(0F, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class).value);
    }

    @Test
    public void test_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertEquals(0F, JSON.parseObject(str, Model.class).value);
        assertEquals(0F, JSON.parseObject(str.toCharArray(), Model.class).value);
        assertEquals(0F, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model.class).value);
    }

    public static class Model {
        public float value;
    }

    @Test
    public void test1() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model1.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model1.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model1.class));
    }

    @Test
    public void test1_array() throws Exception {
        String str = "{\"value\":[]}";
        assertNull(JSON.parseObject(str, Model1.class).value);
        assertNull(JSON.parseObject(str.toCharArray(), Model1.class).value);
        assertNull(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model1.class).value);
    }

    @Test
    public void test1_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertNull(JSON.parseObject(str, Model1.class).value);
        assertNull(JSON.parseObject(str.toCharArray(), Model1.class).value);
        assertNull(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model1.class).value);
    }

    public static class Model1 {
        public Float value;
    }

    @Test
    public void test2() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model2.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model2.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model2.class));
    }

    @Test
    public void test2_array() throws Exception {
        String str = "{\"value\":[]}";
        assertEquals(0D, JSON.parseObject(str, Model2.class).value);
        assertEquals(0D, JSON.parseObject(str.toCharArray(), Model2.class).value);
        assertEquals(0D, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model2.class).value);
    }

    @Test
    public void test2_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertEquals(0D, JSON.parseObject(str, Model2.class).value);
        assertEquals(0D, JSON.parseObject(str.toCharArray(), Model2.class).value);
        assertEquals(0D, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model2.class).value);
    }

    public static class Model2 {
        public double value;
    }

    @Test
    public void test3() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model3.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model3.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model3.class));
    }

    @Test
    public void test3_array() throws Exception {
        String str = "{\"value\":[]}";
        assertNull(JSON.parseObject(str, Model3.class).value);
        assertNull(JSON.parseObject(str.toCharArray(), Model3.class).value);
        assertNull(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model3.class).value);
    }

    @Test
    public void test3_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertNull(JSON.parseObject(str, Model3.class).value);
        assertNull(JSON.parseObject(str.toCharArray(), Model3.class).value);
        assertNull(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model3.class).value);
    }

    public static class Model3 {
        public Double value;
    }

    @Test
    public void test4() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model4.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model4.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model4.class));
    }

    @Test
    public void test4_array() throws Exception {
        String str = "{\"value\":[]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model4.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model4.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model4.class));
    }

    @Test
    public void test4_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertEquals(0, JSON.parseObject(str, Model4.class).value);
        assertEquals(0, JSON.parseObject(str.toCharArray(), Model4.class).value);
        assertEquals(0, JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model4.class).value);
    }

    public static class Model4 {
        public int value;
    }

    @Test
    public void test5() throws Exception {
        String str = "{\"value\":\"ABC\"}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model5.class));
    }

    @Test
    public void test5_array() throws Exception {
        String str = "{\"value\":[]}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model5.class));
    }

    @Test
    public void test5_obj() throws Exception {
        String str = "{\"value\":{}}";
        assertThrows(JSONException.class, () -> JSON.parseObject(str, Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.toCharArray(), Model5.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Model5.class));
    }

    public static class Model5 {
        public long value;
    }
}
