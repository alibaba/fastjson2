package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue711 {
    @Test
    public void test_error_0() {
        String[] strings = new String[] {
                "123a",
        };

        for (String str : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parse(str)
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parse(str.getBytes())
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parse(str.toCharArray())
            );
        }
    }

    @Test
    public void test_error_object_0() {
        String[] strings = new String[] {
                "{}a",
        };

        for (String str : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str)
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.getBytes())
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.toCharArray())
            );
        }
    }

    @Test
    public void test_error_object_1() {
        String[] strings = new String[] {
                "{}a",
        };

        for (String str : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str, 0, str.length())
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.getBytes(), 0, str.length())
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.toCharArray(), 0, str.length())
            );
        }
    }

    @Test
    public void test_error_object_2() {
        String[] strings = new String[] {
                "{}a",
        };

        for (String str : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str, 0, str.length(), Map.class)
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.getBytes(), 0, str.length(), Map.class)
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseObject(str.toCharArray(), 0, str.length(), Map.class)
            );
        }
    }

    @Test
    public void test_error_array_0() {
        String[] strings = new String[] {
                "[]a",
        };

        for (String str : strings) {
            assertThrows(
                    JSONException.class,
                    () -> JSON.parseArray(str)
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseArray(str.getBytes())
            );

            assertThrows(
                    JSONException.class,
                    () -> JSON.parseArray(str.toCharArray())
            );
        }
    }
}
