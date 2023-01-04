package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreNonFieldGetterTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{\"id\":123}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.IgnoreNonFieldGetter));
    }

    public static class Bean {
        public int getId() {
            return 123;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.f0 = 10;
        bean.f1 = 11;
        bean.f2 = 12;

        assertEquals("{\"f0\":10,\"f1\":11,\"f2\":12}", JSON.toJSONString(bean));
        assertEquals("{\"f0\":10,\"f1\":11,\"f2\":12}", JSON.toJSONString(bean, JSONWriter.Feature.IgnoreNonFieldGetter));
    }

    public static class Bean1 {
        public int f1;
        public int f0;
        public int f2;
    }
}
