package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1646 {
    @Test
    public void test() {
        String s = "{\"test\":{null:\"a\",\"b\":\"c\"}}";
        Bean bean = JSONObject.parseObject(s, Bean.class);
        assertEquals("a", bean.getTest().get("null"));
    }

    public class Bean {
        private Map<String, String> test;
        public Map<String, String> getTest() {
            return test;
        }

        public void setTest(Map<String, String> test) {
            this.test = test;
        }
    }
}
