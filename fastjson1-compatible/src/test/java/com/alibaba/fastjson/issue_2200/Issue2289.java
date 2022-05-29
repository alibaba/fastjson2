package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.serializer.JSONSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2289 {
    @Test
    public void test_for_issue() throws Exception {
        B b = new B();
        b.id = 123;

        JSONSerializer jsonSerializer = new JSONSerializer();

        jsonSerializer.writeAs(b, A.class);

        String str = jsonSerializer.toString();
        assertEquals("{}", str);
    }

    public static class A {
    }

    public static class B
            extends A {
        public int id;
    }
}
