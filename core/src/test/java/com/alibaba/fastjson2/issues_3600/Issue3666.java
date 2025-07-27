package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3666 {
    @Test
    public void test() {
        String str = "{\"num\":\"18005509635\",\"fee\":\"2000\"}";
        JSONObject jsonObject = JSON.parseObject(str);
        String result = JSON.toJSONString(jsonObject, new Filter());
        assertEquals("{\"num\":\"****\",\"fee\":\"2000\"}", result);
    }

    public class Filter
            implements ValueFilter {
        @Override
        public Object apply(Object object, String name, Object value) {
            return Collections.singleton("num").contains(name.toLowerCase(Locale.ENGLISH)) ? "****" : value;
        }
    }
}
