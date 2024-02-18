package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1821 {
    @Test
    public void test() {
        String str0 = "[1,,3]";
        char[] char0 = str0.toCharArray();
        byte[] bytes0 = str0.getBytes(StandardCharsets.UTF_8);
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Byte>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Short>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Integer>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Long>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<String>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Float>>() {})
        );
        assertThrows(
                NumberFormatException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<Double>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<BigInteger>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str0,
                        new TypeReference<List<BigDecimal>>() {})
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str0, int[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes0, int[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(char0, int[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str0, Integer[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes0, Integer[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(char0, Integer[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str0, long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes0, long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(char0, long[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str0, Long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes0, Long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(char0, Long[].class)
        );

        String str2 = "[1,k,3]";
        char[] chars2 = str2.toCharArray();
        byte[] bytes2 = str2.getBytes(StandardCharsets.UTF_8);
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(
                        str2, new TypeReference<List<Byte>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<Short>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<Integer>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<Long>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<String>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<Float>>() {})
        );
        assertThrows(
                NumberFormatException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<Double>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<BigInteger>>() {})
        );
        assertThrows(
                JSONException.class, () -> JSON.parseObject(
                        str2,
                        new TypeReference<List<BigDecimal>>() {})
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str2, int[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes2, int[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(chars2, int[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str2, Integer[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes2, Integer[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(chars2, Integer[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str2, long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes2, long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(chars2, long[].class)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str2, Long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(bytes2, Long[].class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(chars2, Long[].class)
        );
    }
}
