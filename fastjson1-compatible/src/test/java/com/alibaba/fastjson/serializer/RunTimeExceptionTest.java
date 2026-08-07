package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RunTimeExceptionTest {
    @Test
    public void test() {
        assertEquals(
                "RuntimeException",
                JSON.parseObject(
                        JSON.toJSONString(new RuntimeException("xxx"))
                ).get("@type")
        );
    }
}
