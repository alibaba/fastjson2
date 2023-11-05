package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1947 {
    @Test
    public void test() {
        A a = new A();
        a.id = 1;
        a.name = "a";

        B b = new B();
        b.id = 2;

        C c = new C();
        c.list = new ID[]{a, b};

        String str = JSON.toJSONString(c);
        assertEquals(
                "{\"list\":[{\"@type\":\"" + Issue1947.class.getName() + "$A\",\"id\":1,\"name\":\"a\"},{\"@type\":\"com.alibaba.fastjson2.issues_1900.Issue1947$B\",\"id\":2}]}",
                str);
    }

    public static class C {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteClassName)
        public ID[] list;
    }

    public static class A
            implements ID {
        public int id;
        public String name;

        @Override
        public int getId() {
            return id;
        }
    }

    public static class B
            implements ID {
        public int id;

        @Override
        public int getId() {
            return id;
        }
    }

    public interface ID {
        int getId();
    }
}
