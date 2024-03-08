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

    @Test
    public void test1() {
        assertEquals(Type1.NONE, JSON.parseObject("{}", Bean1.class).type);
        assertEquals(Type1.NONE, JSON.parseObject("{\"type\":null}", Bean1.class).type);
        assertEquals(Type1.NONE, JSON.parseObject("{\"type\":\"\"}", Bean1.class).type);
    }

    private static class Bean1 {
        @JSONField(defaultValue = "NONE")
        private Type1 type;

        public Type1 getType() {
            return type;
        }

        public void setType(Type1 type) {
            this.type = type;
        }
    }

    private enum Type1 {
        NONE, ONE, TWO
    }

    @Test
    public void test2() {
        assertEquals(Type1.NONE, JSON.parseObject("{}", Bean2.class).type);
        assertEquals(Type1.NONE, JSON.parseObject("{\"type\":null}", Bean2.class).type);
        assertEquals(Type1.NONE, JSON.parseObject("{\"type\":\"\"}", Bean2.class).type);
    }

    private static class Bean2 {
        private final Type1 type;

        private Bean2(@JSONField(defaultValue = "NONE") Type1 type) {
            if (type == null) {
                throw new IllegalArgumentException();
            }
            this.type = type;
        }
    }
}
