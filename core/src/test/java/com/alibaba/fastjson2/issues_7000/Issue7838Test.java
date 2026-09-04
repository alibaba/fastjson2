package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("regression")
public class Issue7838Test {
    @Test
    public void testBooleanFieldRemainsFalseAfterRepeatedParsing() {
        final String json = "{\"flag\":false}";

        for (int i = 0; i < 200_000; i++) {
            FlagBean bean = JSON.parseObject(json, FlagBean.class);
            assertFalse(bean.flag);
        }
    }

    @Test
    public void testRepeatedStringSerialization() {
        final String expected = "{\"id\":\"PREFIX_LONG_CONSTANT_SUFFIX\"}";
        final Event event = new Event();
        for (int i = 0; i < 5_000; i++) {
            assertEquals(expected, JSON.toJSONString(event));
        }
    }

    public static class FlagBean {
        public Boolean flag;
    }

    public static class Event {
        public String getId() {
            return "PREFIX_LONG_CONSTANT_SUFFIX";
        }
    }
}
