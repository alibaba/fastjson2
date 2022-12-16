package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void parseObjectNull() {
        assertNull(JSONB.parseObject(null, A.class, null, new Filter[0]));
        assertNull(JSONB.parseObject(new byte[0], A.class, null, new Filter[0]));
    }

    @Test
    public void parseObject() {
        Int1 bean = new Int1();
        bean.setV0000(100);

        SymbolTable symbolTable = JSONB.symbolTable("v0000");
        assertEquals(1, symbolTable.size());

        byte[] bytes = JSONB.toBytes(bean, symbolTable, new Filter[0]);

        Int1 bean1 = JSONB.parseObject(bytes, Int1.class, symbolTable, new Filter[0]);
        assertEquals(bean.getV0000(), bean1.getV0000());
    }

    @Test
    public void parseObject1() {
        Int1 bean = new Int1();
        bean.setV0000(100);

        SymbolTable symbolTable = JSONB.symbolTable("v0000");

        byte[] bytes = JSONB.toBytes(bean, symbolTable, new Filter[0], JSONWriter.Feature.WriteClassName);

        Int1 bean1 = JSONB.parseObject(bytes, Object.class, symbolTable, new Filter[0], JSONReader.Feature.SupportAutoType);
        assertEquals(bean.getV0000(), bean1.getV0000());
    }

    @Test
    public void symbolTable() {
        SymbolTable symbolTable = JSONB.symbolTable("id", "name");
        assertEquals(2, symbolTable.size());
        assertEquals(hash("id", "name"), symbolTable.hashCode64());
    }

    static long hash(String... items) {
        long hashCode64 = Fnv.MAGIC_HASH_CODE;
        for (String item : items) {
            long hashCode = Fnv.hashCode64(item);
            hashCode64 ^= hashCode;
            hashCode64 *= Fnv.MAGIC_PRIME;
        }
        return hashCode64;
    }

    @Test
    public void nextIfEmptyString() {
        assertTrue(
                JSONReader.ofJSONB(
                            JSONB.toBytes("")
                        )
                        .nextIfNullOrEmptyString()
        );
        assertFalse(JSONReader.ofJSONB(JSONB.toBytes("1")).nextIfNullOrEmptyString());
    }
}
