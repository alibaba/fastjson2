package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExceptionTest {
    @Test
    public void test() {
        new JsonProcessingException();
        new JsonProcessingException("");
        new JsonProcessingException("", new IOException());

        new JsonParseException();
        new JsonParseException("");
        new JsonParseException("", new IOException());
    }
}
