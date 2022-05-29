package com.alibaba.fastjson2.v1issues.issue_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3689 {
    @Test
    public void test_without_type_0_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("dfdfdf");
        });
    }

    @Test
    public void test_without_type_1_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("/dfdfdf");
        });
    }

    @Test
    public void test_without_type_2_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("//dfdfdf");
        });
    }

    @Test
    public void test_without_type_3_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("///dfdfdf");
        });
    }

    @Test
    public void test_without_type_4_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("////dfdfdf");
        });
    }

    @Test
    public void test_without_type_5_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("/////dfdfdf");
        });
    }

    @Test
    public void test_without_type_6_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("//////dfdfdf");
        });
    }

    @Test
    public void test_with_type_0_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_1_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("/dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_2_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("//dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_3_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("///dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_4_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("////dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_5_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("/////dfdfdf", String.class);
        });
    }

    @Test
    public void test_with_type_6_meaningles_char() {
        assertThrows(JSONException.class, () -> {
            JSON.parseArray("//////dfdfdf", String.class);
        });
    }

    @Test
    public void test_for_issue() {
        JSON.parseArray("[\"////dfdfdf\"]"); //不会抛异常
        JSON.parse("[\"dfdfdf\"]"); //不会抛异常
    }
}
