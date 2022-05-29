package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest6 {
    @Test
    public void test_for_issue() throws Exception {
        A a = new A();
        a.value = new C(1001);

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest6$C\",\"id\":1001}}", json);

        A a2 = JSON.parseObject(json, A.class);
        assertSame(a2.value.getClass(), B.class); // autoType not work
    }

    public static class A {
        public B value;
    }

    public static class B {
    }

    public static class C
            extends B {
        public int id;

        public C() {
        }

        public C(int id) {
            this.id = id;
        }
    }
}
