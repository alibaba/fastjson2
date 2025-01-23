package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2535 {
    @Test
    public void test1() {
        String json = "{}";
        List<String> strings = JSON.parseArray(json, String.class);
        assertNotNull(strings);
        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), "{}");
    }

    @Test
    public void test2() {
        String json = "\"11111\"";
        List<String> strings = JSON.parseArray(json, String.class);
        assertNotNull(strings);
        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), "11111");
    }

    @Test
    public void test3() {
        String json = "'22222'";
        List<String> strings = JSON.parseArray(json, String.class);
        assertNotNull(strings);
        assertEquals(strings.size(), 1);
        assertEquals(strings.get(0), "22222");
    }

    @Test
    public void test4() {
        String json = "[]";
        List<String> strings = JSON.parseArray(json, String.class);
        assertNotNull(strings);
        assertEquals(strings.size(), 0);
    }

    @Test
    public void test5() {
        String json = "11\"11111\"";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }

    @Test
    public void test6() {
        String json = "22'22222'";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }

    @Test
    public void test7() {
        String json = "1{}";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }

    @Test
    public void test8() {
        String json = "a{}";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }

    @Test
    public void test9() {
        String json = "a'333'";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }

    @Test
    public void test10() {
        String json = "a\"444\"";
        assertThrows(JSONException.class, () -> JSON.parseArray(json, String.class));
    }
}
