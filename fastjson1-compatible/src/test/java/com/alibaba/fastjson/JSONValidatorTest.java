package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONValidatorTest {
    @Test
    public void validate_test_accurate() throws Throwable {
        boolean isValidate = JSONValidator.from("{\"string\":\"a\",\"nums\":[0,-1,10,0.123,1e5,-1e+6,0.1e-7],\"object\":{\"empty\":{},\"list\":[]},\"list\":[\"object\",{\"true\":true,\"false\":false,\"null\":null}]}").validate();
        assertTrue(isValidate);
    }

    @Test
    public void fromUtf() {
        assertTrue(JSONValidator.fromUtf8("{\"id\":123}".getBytes(StandardCharsets.UTF_8)).validate());
    }
}
