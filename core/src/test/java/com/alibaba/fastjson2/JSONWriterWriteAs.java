package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterWriteAs {
    @Test
    public void test() {
        B b = new B();
        b.id = 123;
        b.name = "abc";

        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeAs(b, A.class);
        assertEquals("{\"id\":123}", jsonWriter.toString());
    }

    @Test
    public void testNull() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeAs(null, A.class);
        assertEquals("null", jsonWriter.toString());
    }

    public static class A {
        public int id;
    }

    public static class B
            extends A {
        public String name;
    }
}
