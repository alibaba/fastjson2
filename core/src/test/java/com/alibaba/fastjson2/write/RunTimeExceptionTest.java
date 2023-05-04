package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RunTimeExceptionTest {
    @Test
    public void test() {
        assertEquals(
                "RuntimeException",
                JSON.parseObject(
                        JSON.toJSONString(
                                new RuntimeException("xxx"),
                                JSONWriter.Feature.WriteClassName
                        )
                ).get("@type")
        );
        assertEquals(
                "RuntimeException",
                JSON.parseObject(
                        JSON.toJSONString(
                                new RuntimeException("xxx"),
                                JSONWriter.Feature.WriteThrowableClassName
                        )
                ).get("@type")
        );
        assertNull(
                JSON.parseObject(
                        JSON.toJSONString(new RuntimeException("xxx"))
                ).get("@type")
        );
    }
}
