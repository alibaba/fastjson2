package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest3 {

    @Test
    public void test0() {
        assertEquals(
                123,
                JSONObject
                        .of("bean", JSONObject.of("id", 123))
                        .getObject("bean", Bean::new)
                        .id
        );
    }

    @Test
    public void test1() {
        assertEquals(
                123,
                JSONArray
                        .of(JSONObject.of("id", 123))
                        .getObject(0, Bean::new)
                        .id
        );
    }

    public static class Bean {
        public int id;

        public Bean(JSONObject input) {
            this.id = input.getIntValue("id");
        }
    }
}
