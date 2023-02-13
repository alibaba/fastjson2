package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1111 {
    @Test
    public void test() {
        assertEquals(
                "{\"value\":\"161127097456177153\"}",
                JSON.toJSONString(
                        JSONObject.of("value", 161127097456177153L),
                        JSONWriter.Feature.BrowserCompatible
                )
        );
    }

    @Test
    public void testBean() {
        Bean bean = new Bean();
        bean.value = 161127097456177153L;
        assertEquals(
                "{\"value\":\"161127097456177153\"}",
                JSON.toJSONString(bean, JSONWriter.Feature.BrowserCompatible)
        );
    }

    public static class Bean {
        public long value;
    }

    @Test
    public void testBean1() {
        Bean1 bean = new Bean1();
        bean.value = 161127097456177153L;
        assertEquals(
                "{\"value\":\"161127097456177153\"}",
                JSON.toJSONString(bean, JSONWriter.Feature.BrowserCompatible)
        );
    }

    public static class Bean1 {
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
