package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SQLJSONTest {
    @Test
    public void test() {
        assertEquals(
                101,
                JSONObject
                        .of("id", 101)
                        .eval(
                                JSONPath.of("strict $.id")
                        )
        );
        assertEquals(
                101,
                JSONObject
                        .of("id", 101)
                        .eval(
                                JSONPath.of("lax $.id")
                        )
        );
    }
}
