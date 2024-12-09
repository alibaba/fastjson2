package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBDumTest {
    @Test
    public void test_0() throws Exception {
        Int1 vo = new Int1();

        byte[] jsonbBytes = JSONB.toBytes(
                vo,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.IgnoreErrorGetter);

        JSONB.dump(jsonbBytes);

        assertEquals("{\n" +
                "\t\"@type\": \"com.alibaba.fastjson2_vo.Int1\"\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test_1() throws Exception {
        Int1 vo = new Int1();

        byte[] jsonbBytes = JSONB.toBytes(
                vo,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.IgnoreErrorGetter);

        JSONB.dump(jsonbBytes);

        assertEquals("{}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test_2() {
        SymbolTable symbolTable = JSONB.symbolTable("id");
        Bean bean = new Bean();
        bean.id = 123;
        byte[] bytes = JSONB.toBytes(bean, symbolTable);
        String str = JSONB.toJSONString(bytes, symbolTable);
        assertEquals("{\n" +
                "\t\"id\": 123\n" +
                "}", str);
    }

    public static class Bean {
        public int id;
    }

    @Test
    public void test_3() {
        String str = "123456789012345678901234567890123456789012345678901234567890";
        byte[] bytes = JSONB.toBytes(new BigInteger(str));
        assertEquals(str, JSONB.toJSONString(bytes));
    }

    @Test
    public void test_4() {
        Map map = new TreeMap();
        map.put("id", null);
        byte[] jsonbBytes = JSONB.toBytes(map, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\": \"TreeMap\",\n" +
                "\t\"id\": null\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void dec() {
        String str = "1234567890123456789012345678901234567890123456789.01234567890";
        byte[] bytes = JSONB.toBytes(new BigDecimal(str));
        assertEquals(str, JSONB.toJSONString(bytes));
    }

    @Test
    public void test_typed() {
        Object[] array = new Object[] {
                new TreeMap(),
                new TreeMap(),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>()
        };
        byte[] jsonbBytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\": \"[O\",\n" +
                "\t\"@value\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"@type\": \"TreeMap\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"@type\": \"TreeMap\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"@type\": \"ConcurrentHashMap\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"@type\": \"ConcurrentHashMap\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test_typed_1() {
        TreeMap k1 = new TreeMap<>();
        k1.put("v", 1);
        TreeMap k2 = new TreeMap<>();
        k2.put("v", 2);
        JSONObject object = JSONObject.of(
                "k1", k1,
                "k2", k2,
                "k3", new ConcurrentHashMap<>(),
                "k4", new ConcurrentHashMap<>()
        );
        byte[] jsonbBytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\": \"JSONObject\",\n" +
                "\t\"k1\": {\n" +
                "\t\t\"@type\": \"TreeMap\",\n" +
                "\t\t\"v\": 1\n" +
                "\t},\n" +
                "\t\"k2\": {\n" +
                "\t\t\"@type\": \"TreeMap\",\n" +
                "\t\t\"v\": 2\n" +
                "\t},\n" +
                "\t\"k3\": {\n" +
                "\t\t\"@type\": \"ConcurrentHashMap\"\n" +
                "\t},\n" +
                "\t\"k4\": {\n" +
                "\t\t\"@type\": \"ConcurrentHashMap\"\n" +
                "\t}\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void bigint() {
        BigInteger[] values = new BigInteger[40];
        long v = 1;
        for (int i = 0; i < 20; i++) {
            v *= 10;
            values[i * 2] = BigInteger.valueOf(v);
            values[i * 2 + 1] = BigInteger.valueOf(-v);
        }
        byte[] bytes = JSONB.toBytes(values);
        String dumpStr = JSONB.toJSONString(bytes);
        assertArrayEquals(values, JSON.parseObject(dumpStr, BigInteger[].class));
    }

    @Test
    public void dec1() {
        BigDecimal[] values = new BigDecimal[40];
        long v = 1;
        for (int i = 0; i < 20; i++) {
            v *= 10;
            values[i * 2] = BigDecimal.valueOf(v);
            values[i * 2 + 1] = BigDecimal.valueOf(-v);
        }
        byte[] bytes = JSONB.toBytes(values);
        String dumpStr = JSONB.toJSONString(bytes);
        assertArrayEquals(values, JSON.parseObject(dumpStr, BigDecimal[].class));
    }
}
