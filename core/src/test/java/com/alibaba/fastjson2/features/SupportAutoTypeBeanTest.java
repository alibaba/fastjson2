package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SupportAutoTypeBeanTest {
    @Test
    public void test() {
        A a = new B();
        String str = JSON.toJSONString(a, JSONWriter.Feature.WriteClassName);
        A a1 = JSON.parseObject(str, A.class);
        assertEquals(a.getClass(), a1.getClass());
    }

    @JSONType(deserializeFeatures = JSONReader.Feature.SupportAutoType)
    public static class A {
    }

    public static class B
            extends A {
    }
}
