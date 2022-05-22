package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest5 {
    @Test
    public void test_for_issue() throws Exception {
        A a = new A();
        a.value = new C(1001);

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest5$C\",\"id\":1001}}", json);

        A a1 = JSON.parseObject(json, A.class);
        assertEquals(A.class, a1.getClass());
    }

    public static class A {
        public B value;
    }

    public static class B {
        public B(int type) {
        }
    }

    public static class C
            extends B {
        public int id;

        public C() {
            super(0);
        }

        public C(int id) {
            super(0);
            this.id = id;
        }
    }
}
