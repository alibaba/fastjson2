package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectFluentTest {
    @Test
    public void test_fluent() throws Exception {
        JSONObject object = new JSONObject() //
                .fluentPut("1", 1001) //
                .fluentPut("2", 1002);

        assertEquals(2, object.size());

        object.fluentPutAll(Collections.singletonMap("3", 1003)) //
                .fluentPutAll(Collections.singletonMap("4", 1004));

        assertEquals(4, object.size());

        object.fluentRemove("1") //
                .fluentRemove("2");

        assertEquals(2, object.size());

        object.fluentClear().fluentClear();

        assertEquals(0, object.size());
    }
}
