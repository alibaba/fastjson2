package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypedMapTest {
    @Test
    public void test_0_jsonb() {
        A a = new A();
        Map<String, String> map = new HashMap<>();
        map.put("id", "1001");
        map.put("tag", null);
        map.put("name", "DataWorks");
        a.setData(map);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
        A a1 = (A) JSONB.parse(bytes, JSONReader.Feature.SupportAutoType);
        assertEquals(2, a1.data.size());
        assertEquals("1001", a1.data.get("id"));
        assertEquals("DataWorks", a1.data.get("name"));
    }

    @Test
    public void test_0() {
        A a = new A();
        Map<String, String> map = new HashMap<>();
        map.put("id", "1001");
        map.put("tag", null);
        map.put("name", "DataWorks");
        a.setData(map);

        String str = JSON.toJSONString(a, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteClassName);
        A a1 = (A) JSON.parse(str, JSONReader.Feature.SupportAutoType);
        assertEquals(2, a1.data.size());
        assertEquals("1001", a1.data.get("id"));
        assertEquals("DataWorks", a1.data.get("name"));
    }

    public static class A {
        public Map<String, String> data;

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }
}
