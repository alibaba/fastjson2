package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Issue1078 {
    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "b");

        assertEquals(
                "{\"a\":\"b12\"}",
                JSON.toJSONString(
                        map,
                        new ValueFilter[]{new TestValueFilter1(), new TestValueFilter2()},
                        JSONWriter.Feature.WriteMapNullValue
                )
        );

        assertEquals(
                "{\"a\":\"b21\"}",
                JSON.toJSONString(
                        map,
                        new ValueFilter[]{new TestValueFilter2(), new TestValueFilter1()},
                        JSONWriter.Feature.WriteMapNullValue
                )
        );
    }

    public static class TestValueFilter1
            implements ValueFilter {
        @Override
        public Object apply(Object o, String s, Object o1) {
            return o1.toString() + "1";
        }
    }

    public static class TestValueFilter2
            implements ValueFilter {
        @Override
        public Object apply(Object o, String s, Object o1) {
            return o1.toString() + "2";
        }
    }
}
