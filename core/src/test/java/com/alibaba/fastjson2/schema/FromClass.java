package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2.util.Differ;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FromClass {
    @Test
    public void test() {
        JSONSchema schema = JSONSchema.of(Bean.class);
        String string = schema.toString();
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"}},\"required\":[\"id\"]}",
                string
        );
        JSONSchema pased = JSONSchema.of(JSON.parseObject(string));
        assertTrue(Differ.diff(schema, pased));

        Bean bean = new Bean();
        JSONSchema valueSchema = JSONSchema.ofValue(bean);
        assertTrue(Differ.diff(schema, valueSchema));
    }

    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void testSimpleType() {
        assertEquals("{\"type\":\"boolean\"}", JSONSchema.of(boolean.class).toString());
        assertEquals("{\"type\":\"boolean\"}", JSONSchema.of(Boolean.class).toString());
        assertEquals("{\"type\":\"boolean\"}", JSONSchema.of(AtomicBoolean.class).toString());

        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(byte.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(short.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(int.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(long.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(Byte.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(Short.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(Integer.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(Long.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(BigInteger.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(AtomicInteger.class).toString());
        assertEquals("{\"type\":\"integer\"}", JSONSchema.of(AtomicLong.class).toString());

        assertEquals("{\"type\":\"number\"}", JSONSchema.of(float.class).toString());
        assertEquals("{\"type\":\"number\"}", JSONSchema.of(double.class).toString());
        assertEquals("{\"type\":\"number\"}", JSONSchema.of(Float.class).toString());
        assertEquals("{\"type\":\"number\"}", JSONSchema.of(Double.class).toString());
        assertEquals("{\"type\":\"number\"}", JSONSchema.of(BigDecimal.class).toString());

        assertEquals("{\"type\":\"string\"}", JSONSchema.of(String.class).toString());
    }

    @Test
    public void testListType() {
        assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"boolean\"}}", JSONSchema.of(TypeReference.collectionType(Collection.class, Boolean.class)).toString());
        assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"boolean\"}}", JSONSchema.of(TypeReference.arrayType(Boolean.class)).toString());
        assertEquals("{\"type\":\"array\",\"items\":{\"type\":\"boolean\"}}", JSONSchema.of(boolean[].class).toString());
        assertEquals("{\"type\":\"array\"}", JSONSchema.of(Collection.class).toString());
    }

    @Test
    public void testMapType() {
        assertEquals("{\"type\":\"object\"}", JSONSchema.of(Map.class).toString());
        assertEquals("{\"type\":\"object\"}", JSONSchema.of(TypeReference.mapType(Map.class, String.class, Long.class)).toString());
    }

    @Test
    public void testEishay() {
        JSONSchema schema = JSONSchema.of(MediaContent.class);
        String string = schema.toString();
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"images\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"height\":{\"type\":\"integer\"},\"size\":{\"type\":\"string\",\"enum\":[\"SMALL\",\"LARGE\"]},\"title\":{\"type\":\"string\"},\"uri\":{\"type\":\"string\"},\"width\":{\"type\":\"integer\"}},\"required\":[\"height\",\"width\"]}},\"media\":{\"type\":\"object\",\"properties\":{\"bitrate\":{\"type\":\"integer\"},\"copyright\":{\"type\":\"string\"},\"duration\":{\"type\":\"integer\"},\"format\":{\"type\":\"string\"},\"height\":{\"type\":\"integer\"},\"persons\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"player\":{\"type\":\"string\",\"enum\":[\"JAVA\",\"FLASH\"]},\"size\":{\"type\":\"integer\"},\"title\":{\"type\":\"string\"},\"uri\":{\"type\":\"string\"},\"width\":{\"type\":\"integer\"}},\"required\":[\"bitrate\",\"duration\",\"height\",\"size\",\"width\"]}}}",
                string
        );
        JSONSchema pased = JSONSchema.of(JSON.parseObject(string));
        assertTrue(Differ.diff(schema, pased));
    }

    @Test
    public void fromValueMap() {
        Map map = new HashMap();
        map.put("id", 123);
        assertEquals("{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}}}", JSONSchema.ofValue(map).toString());
    }

    @Test
    public void fromValueList() {
        List list = new ArrayList();
        list.add("xxx");
        assertEquals("{\"type\":\"array\"}", JSONSchema.ofValue(list).toString());
    }
}
