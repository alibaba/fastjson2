package com.alibaba.fastjson2.trino;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.support.airlift.SliceValueConsumer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SliceValueConsumerTest {
    @Test
    public void test_str() {
        String json = "{\"value\":\"999\"}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("999", consumer.slice.toStringAscii());
    }

    @Test
    public void test_str1() {
        String json = "{\"value\":\"999\\\"a\"}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("999\"a", consumer.slice.toStringAscii());
    }

    @Test
    public void test_str2() {
        String json = "{\"value\":\"阿里巴巴\"}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("阿里巴巴", consumer.slice.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void test_str2_ut8() {
        String json = "{\"value\":\"阿里巴巴\"}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json.getBytes(StandardCharsets.UTF_8)), consumer);
        assertEquals("阿里巴巴", consumer.slice.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void test_int() {
        String json = "{\"value\":999}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("999", consumer.slice.toStringAscii());
    }

    @Test
    public void test_int_utf8() {
        String json = "{\"value\":999}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json.getBytes(StandardCharsets.UTF_8)), consumer);
        assertEquals("999", consumer.slice.toStringAscii());
    }

    @Test
    public void test_long() {
        String json = "{\"value\":123456789012345678901234567890}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("123456789012345678901234567890", consumer.slice.toStringAscii());
    }

    @Test
    public void test_long_utf8() {
        String json = "{\"value\":123456789012345678901234567890}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json.getBytes(StandardCharsets.UTF_8)), consumer);
        assertEquals("123456789012345678901234567890", consumer.slice.toStringAscii());
    }

    @Test
    public void test_null() {
        String json = "{\"value\":123456789012345678901234567890}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value1").extractScalar(JSONReader.of(json), consumer);
        assertEquals(null, consumer.slice);
    }

    @Test
    public void test_true() {
        String json = "{\"value\":true}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("true", consumer.slice.toStringAscii());
    }

    @Test
    public void test_false() {
        String json = "{\"value\":false}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("false", consumer.slice.toStringAscii());
    }

    @Test
    public void test_object_empty() {
        String json = "{\"value\":{}}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("{}", consumer.slice.toStringAscii());
    }

    @Test
    public void test_object() {
        String json = "{\"value\":{\"id\":1}}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("{\"id\":1}", consumer.slice.toStringAscii());
    }

    @Test
    public void test_array_empty() {
        String json = "{\"value\":[]}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("[]", consumer.slice.toStringAscii());
    }

    @Test
    public void test_array() {
        String json = "{\"value\":[1]}";
        SliceValueConsumer consumer = new SliceValueConsumer();
        JSONPath.of("$.value").extractScalar(JSONReader.of(json), consumer);
        assertEquals("[1]", consumer.slice.toStringAscii());
    }
}
