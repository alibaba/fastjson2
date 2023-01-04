package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue866 {
    @Test
    public void test() {
        JSON.register(Long.class, new ObjectWriter<Long>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                Long value = (Long) object;
                if (value == null) {
                    jsonWriter.writeNull();
                } else {
                    jsonWriter.writeString(Long.toString(value));
                }
            }
        });

        try {
            Map<String, Long> map = new HashMap<>();
            map.put("0", 0L);
            String s = JSON.toJSONString(map);
            assertEquals("{\"0\":\"0\"}", s);

            List list = JSONArray.of(0L);
            assertEquals(
                    "[\"0\"]",
                    JSON.toJSONString(list)
            );
        } finally {
            JSON.register(Long.class, (ObjectWriter) null);
        }
    }

    @Test
    public void test1() {
        Map<String, Long> map = new HashMap<>();
        map.put("0", 0L);
        String s = JSON.toJSONString(map, JSONWriter.Feature.WriteLongAsString);
        assertEquals("{\"0\":\"0\"}", s);
    }

    @Test
    public void test2() {
        List list = JSONArray.of(0L);
        String s = JSON.toJSONString(list, JSONWriter.Feature.WriteLongAsString);
        assertEquals("[\"0\"]", s);
    }
}
