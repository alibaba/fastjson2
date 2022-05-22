package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest2 {
    @Test
    public void test_for_issue() throws Exception {
        A a = new A();
        a.list.add(new C(1001));
        a.list.add(new C(1002));

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"list\":[{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest2$C\",\"id\":1001},{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest2$C\",\"id\":1002}]}", json);

        A a2 = JSON.parseObject(json, A.class, JSONReader.Feature.SupportAutoType);
        assertSame(a2.list.get(0).getClass(), C.class);
    }

    public static class A {
        public List<B> list = new ArrayList<B>();
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
