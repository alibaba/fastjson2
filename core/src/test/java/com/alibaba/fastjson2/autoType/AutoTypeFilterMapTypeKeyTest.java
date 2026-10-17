package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeFilterMapTypeKeyTest {
    @Test
    public void test() {
        String str = "{\"@type\":\"" + Bean.class.getName() + "\""
                + ",\"attrs\":{\"@type\":\"java.util.HashMap\",\"k\":1}"
                + ",\"labels\":{\"@type\":\"java.util.HashMap\",\"p\":\"q\"}}";

        Bean bean = (Bean) JSON.parseObject(str, Object.class, JSONReader.autoTypeFilter(Bean.class.getName()));

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("k", 1);
        assertEquals(attrs, bean.attrs);

        Map<String, String> labels = new HashMap<>();
        labels.put("p", "q");
        assertEquals(labels, bean.labels);
    }

    public static class Bean {
        public Map<String, Object> attrs;
        public Map<String, String> labels;
    }
}
