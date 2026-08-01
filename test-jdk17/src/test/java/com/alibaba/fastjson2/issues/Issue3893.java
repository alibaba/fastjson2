package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3893 {
    @Test
    public void test() {
        Foo foo = new Foo(5);
        Object json = JSON.toJSON(foo);
        assertEquals("{\"x\":5,\"y\":42}", json.toString());
    }

    record Foo(int x) {
        @JsonProperty
        public int y() {
            return 42;
        }
    }
}
