package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue709 {
    @Test
    public void test_Integer() {
        String str = "[1, ]";
        List<Integer> list = JSON.parseArray(str, Integer.class);
        assertEquals(1, list.size());
        assertEquals(1, list.get(0));
    }

    @Test
    public void test_Long() {
        String str = "[1, ]";
        List<Long> list = JSON.parseArray(str, Long.class);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0));
    }

    @Test
    public void test_error() {
        String str = "[1,";
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, Integer.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, Long.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, Float.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, Double.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, Number.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str, String.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(str)
        );

        char[] chars = str.toCharArray();
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, Integer.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, Long.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, Float.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, Double.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, Number.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars, String.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(chars)
        );

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, Integer.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, Long.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, Float.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, Double.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, Number.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, String.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes)
        );

        assertThrows(
                JSONException.class,
                () -> JSON.parseArray(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
        );

        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray()
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(Integer.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(Long.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(Float.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(Double.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(Number.class)
        );
        assertThrows(
                JSONException.class,
                () -> JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .readArray(String.class)
        );

        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray()
        );
        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray(Integer.class)
        );
        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray(Long.class)
        );
        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray(Float.class)
        );
        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray(Double.class)
        );
        assertThrows(
                JSONException.class,
                () -> new JSONReaderStr(str)
                        .readArray(String.class)
        );
    }

    @Test
    public void test_error_1() {
        String str = "[1,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            assertThrows(
                    JSONException.class,
                    () -> jsonReader.read(new JSONArray())
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            assertThrows(
                    JSONException.class,
                    () -> jsonReader.readArray(new JSONArray(), Integer.class)
            );
        }

        Type[] types = new Type[]{
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Number.class,
                BigDecimal.class,
                BigInteger.class,
                Float.class,
                Double.class,
                String.class
        };

        for (Type type : types) {
            for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
                assertThrows(
                        JSONException.class,
                        () -> jsonReader.readArray(type)
                );
            }
        }
    }

    @Test
    public void test_error_2() {
        String str = "[null,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            assertThrows(
                    JSONException.class,
                    () -> jsonReader.read(new JSONArray())
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            assertThrows(
                    JSONException.class,
                    () -> jsonReader.readArray(new JSONArray(), Integer.class)
            );
        }

        Type[] types = new Type[]{
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Number.class,
                BigDecimal.class,
                BigInteger.class,
                Float.class,
                Double.class,
                String.class
        };

        for (Type type : types) {
            for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
                assertThrows(
                        JSONException.class,
                        () -> jsonReader.readArray(type)
                );
            }
        }
    }

    @Test
    public void test_error_4() {
        String[] strings = new String[] {
                "{\"id\":null,",
                "{\"id\":null",
                "{\"id\":",
                "{",
                "{\"",
        };

        for (String str : strings) {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders4(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertThrows(
                        JSONException.class,
                        () -> jsonReader.readObject()
                );
            }
        }
    }
}
