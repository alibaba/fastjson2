package com.alibaba.fastjson.issue_3600;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3689 {
    @Test
    public void test_without_type_0_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("dfdfdf");
        });
    }

    @Test
    public void test_without_type_1_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("/dfdfdf");
        });
    }

    @Test
    public void test_without_type_2_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("//dfdfdf");
        });
    }

    @Test
    public void test_without_type_3_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("///dfdfdf");
        });
    }

    @Test
    public void test_without_type_4_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("////dfdfdf");
        });
    }

    @Test
    public void test_without_type_5_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("/////dfdfdf");
        });
    }

    @Test
    public void test_without_type_6_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("//////dfdfdf");
        });
    }

    @Test
    public void test_with_type_0_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_1_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("/dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_2_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("//dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_3_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("///dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_4_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("////dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_5_meaningles_char() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> {
            JSONArray.parseArray("/////dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_6_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSONArray.parseArray("//////dfdfdf", String.class);
        });
    }

    @Test
    public void test_for_issue() {
        JSONArray.parseArray("[\"////dfdfdf\"]"); //不会抛异常
        JSONArray objects = JSONArray.parseArray("[\"dfdfdf\"]"); //不会抛异常
        System.out.println(JSONArray.parseArray("[\"////dfdfdf\"]"));
        System.out.println(JSONArray.parseArray("[\"dfdfdf\"]"));
    }
}
