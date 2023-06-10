package com.alibaba.fastjson2.mixins;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadMixin {
    @Test
    public void test() {
        JSON.mixIn(Bean.class, BeanMixin.class);
        Bean bean = JSON.parseObject("{\"id\":123}", Bean.class);
        assertEquals(123, bean.id);
    }

    public static class Bean {
        private int id;

        public void id(int id) {
            this.id = id;
        }
    }

    public interface BeanMixin {
        @JSONField
        void id(int id);
    }

    @Test
    public void test1() {
        JSON.mixIn(Bean1.class, BeanMixin1.class);
        Bean1 bean = JSON.parseObject("{\"id\":123,\"name\":\"abc\"}", Bean1.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.name);
    }

    static class Bean1 {
        private int id;
        private String name;

        public Bean1(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public void name(String name) {
            this.name = name;
        }
    }

    public interface BeanMixin1 {
        @JSONField
        void name(String name);
    }
}
