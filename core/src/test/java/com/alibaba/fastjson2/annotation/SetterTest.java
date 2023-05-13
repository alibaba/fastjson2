package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetterTest {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"id\":123}", Bean.class);
        assertEquals(123, bean.id);
        assertEquals("{\"id\":123}", JSON.toJSONString(bean));
    }

    public static class Bean {
        private int id;

        @JSONField
        public void id(int id) {
            this.id = id;
        }

        @JSONField
        public int id() {
            return id;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"get\":123}", Bean1.class);
        assertEquals(123, bean.get);
        assertEquals("{\"get\":123}", JSON.toJSONString(bean));
    }

    public static class Bean1 {
        private int get;

        @JSONField
        public void get(int get) {
            this.get = get;
        }

        @JSONField
        public int get() {
            return get;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"set\":123}", Bean2.class);
        assertEquals(123, bean.set);
        assertEquals("{\"set\":123}", JSON.toJSONString(bean));
    }

    public static class Bean2 {
        private int set;

        @JSONField
        public void set(int set) {
            this.set = set;
        }

        @JSONField
        public int set() {
            return set;
        }
    }
}
