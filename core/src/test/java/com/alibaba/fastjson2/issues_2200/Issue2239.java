package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2239 {
    @Test
    public void test() {
        assertEquals(Type.NONE, JSON.parseObject("{}", Bean.class).type);
        assertEquals(Type.NONE, JSON.parseObject("{\"type\":null}", Bean.class).type);
        assertEquals(Type.NONE, JSON.parseObject("{\"type\":\"\"}", Bean.class).type);
    }

    public static class Bean {
        @JSONField(defaultValue = "NONE")
        public Type type;
    }

    public enum Type {
        NONE, ONE, TWO
    }
}
