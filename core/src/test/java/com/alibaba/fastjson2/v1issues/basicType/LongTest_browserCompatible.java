package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongTest_browserCompatible {
    @Test
    public void test_array() throws Exception {
        long[] values = new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        String text = JSON.toJSONString(values, JSONWriter.Feature.BrowserCompatible);
        long[] values_2 = JSON.parseObject(text, long[].class);
        assertEquals(values_2.length, values.length);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i], values_2[i]);
        }
    }

//    public void test_array_writer() throws Exception {
//        long[] values = new long[] {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
//
//        StringWriter writer = new StringWriter();
//        JSON.writeJSONString(writer, values, SerializerFeature.BrowserCompatible);
//        String text = writer.toString();
//        long[] values_2 = JSON.parseObject(text, long[].class);
//        assertEquals(values_2.length, values.length);
//        for (int i = 0; i < values.length; ++i) {
//            assertEquals(values[i], values_2[i]);
//        }
//    }
//
//    public void test_array_writer_2() throws Exception {
//        Random random = new Random();
//        long[] values = new long[2048];
//        for (int i = 0; i < values.length; ++i) {
//            values[i] = random.nextLong();
//        }
//
//        StringWriter writer = new StringWriter();
//        JSON.writeJSONString(writer, values, SerializerFeature.BrowserCompatible);
//        String text = writer.toString();
//        long[] values_2 = JSON.parseObject(text, long[].class);
//        assertEquals(values_2.length, values.length);
//        for (int i = 0; i < values.length; ++i) {
//            assertEquals(values[i], values_2[i]);
//        }
//    }

    @Test
    public void test_map() throws Exception {
        long[] values = new long[]{Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < values.length; ++i) {
            map.put(Long.toString(i), values[i]);
        }

        String text = JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible);
        JSONObject obj = JSON.parseObject(text);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i], ((Number) obj.getLong(Long.toString(i))).longValue());
        }
    }
}
