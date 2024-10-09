package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue2566 {
    @Test
    public void test() {
        // Testing with incorrect JSON syntax
        assertFalse(
                JSON.isValid("{\"name\":\"999}")
        );
        assertFalse(
                JSONValidator.from("{\"name\":\"999}")
                        .validate());
    }
}
