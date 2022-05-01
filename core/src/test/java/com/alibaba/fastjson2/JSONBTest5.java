package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONBTest5 {
    @Test
    public void test_0() {
        String key = "com.taobao.component.api.response.ComponentProtocol12345679012345679012356790";
        JSONObject object = JSONObject.of(key, 123);
        byte[] jsonbBytes = JSONB.toBytes(object);
        JSONObject object1 = JSONB.parseObject(jsonbBytes);
        assertEquals(object1, object);

        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
        assertTrue(jsonReader.nextIfObjectStart());
        assertEquals(Fnv.hashCode64(key), jsonReader.readFieldNameHashCode());
        assertEquals(key, jsonReader.getFieldName());
    }

    @Test
    public void test_1() {
        String key = "com.taobao.component.api.response.ComponentProtocol12345679012345679012356790";
        JSONObject object = JSONObject.of("@type", key);
        byte[] jsonbBytes = JSONB.toBytes(object);
        JSONObject object1 = JSONB.parseObject(jsonbBytes);
        assertEquals(object1, object);

        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
        assertTrue(jsonReader.nextIfObjectStart());
        assertEquals(Fnv.hashCode64("@type"), jsonReader.readFieldNameHashCode());
        assertEquals("@type", jsonReader.getFieldName());
        assertEquals(Fnv.hashCode64(key), jsonReader.readValueHashCode());
        assertEquals(key, jsonReader.getString());
        assertTrue(jsonReader.nextIfObjectEnd());
    }

    @Test
    public void test_2() {
        A a = JSONB.parseObject(JSONB.toBytes(new JSONObject()), A.class);
    }

    @Test
    public void test_3() {
        JSONObject object = JSONObject.of("姓名", "张三");
        byte[] bytes = JSONB.toBytes(object);
        assertEquals(object, JSONB.parseObject(bytes));
    }

    @Test
    public void test_3_ascii() {
        JSONObject object = JSONObject.of("name", "gaotie");
        byte[] bytes = JSONB.toBytes(object);
        assertEquals(object, JSONB.parseObject(bytes));
    }

    @Test
    public void test_3_utf8() {
        String key = "姓名";
        String value = "张三";
        JSONObject object = JSONObject.of(key, value);

        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();
        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        jsonWriter.writeInt32(keyBytes.length);
        jsonWriter.writeRaw(keyBytes);

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF8);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        jsonWriter.writeInt32(valueBytes.length);
        jsonWriter.writeRaw(valueBytes);

        jsonWriter.endObject();

        byte[] bytes = jsonWriter.getBytes();
        assertEquals(object, JSONB.parseObject(bytes));
    }

    @Test
    public void test_3_utf16() {
        String key = "姓名";
        String value = "张三";
        JSONObject object = JSONObject.of(key, value);

        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16);
        jsonWriter.writeInt32(keyBytes.length);
        jsonWriter.writeRaw(keyBytes);

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_16);
        jsonWriter.writeInt32(valueBytes.length);
        jsonWriter.writeRaw(valueBytes);

        jsonWriter.endObject();

        byte[] bytes = jsonWriter.getBytes();
        assertEquals(object, JSONB.parseObject(bytes));
    }

    @Test
    public void test_3_utf16_be() {
        String key = "姓名";
        String value = "张三";
        JSONObject object = JSONObject.of(key, value);

        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16BE);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16BE);
        jsonWriter.writeInt32(keyBytes.length);
        jsonWriter.writeRaw(keyBytes);

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16BE);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_16BE);
        jsonWriter.writeInt32(valueBytes.length);
        jsonWriter.writeRaw(valueBytes);

        jsonWriter.endObject();

        byte[] bytes = jsonWriter.getBytes();
        assertEquals(object, JSONB.parseObject(bytes));
    }

    @Test
    public void test_3_utf16_le() {
        String key = "姓名";
        String value = "张三";
        JSONObject object = JSONObject.of(key, value);

        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16LE);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16LE);
        jsonWriter.writeInt32(keyBytes.length);
        jsonWriter.writeRaw(keyBytes);

        jsonWriter.writeRaw(JSONB.Constants.BC_STR_UTF16LE);
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_16LE);
        jsonWriter.writeInt32(valueBytes.length);
        jsonWriter.writeRaw(valueBytes);

        jsonWriter.endObject();

        byte[] bytes = jsonWriter.getBytes();
        assertEquals(object, JSONB.parseObject(bytes));
    }

    public static class A {
        private A() {

        }
    }
}
