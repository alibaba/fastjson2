package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTypeAlphabetic {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = 101;
        bean.f1 = 102;
        bean.f2 = 103;
        bean.f3 = 104;
        String json = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(json, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
    }

    @JSONType(alphabetic = false)
    public static class Bean {
        public int f3;
        public int f1;
        public int f2;
        public int f0;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.f0 = 101;
        bean.f1 = 102;
        bean.f2 = 103;
        bean.f3 = 104;
        String json = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(json, Bean1.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
        assertEquals(bean.f2, bean1.f2);
        assertEquals(bean.f3, bean1.f3);
    }

    @JSONType(alphabetic = false)
    private static class Bean1 {
        public int f3;
        public int f1;
        public int f2;
        public int f0;
    }
}
