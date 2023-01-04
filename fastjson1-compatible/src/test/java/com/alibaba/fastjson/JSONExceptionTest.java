package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONExceptionTest {
    @Test
    public void test() {
        JSONException exception = new JSONException();
        assertNull(exception.getMessage());
    }
}
