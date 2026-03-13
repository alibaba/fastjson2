package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("regression")
public class Issue28 {
    @Test
    public void test_generic() {
        String str = "{}";

        Result result = JSON.parseObject(str, new TypeReference<Result>() {
        });
        assertNotNull(result);
    }

    @Test
    public void test_generic_1() {
        String str = "{}";

        Result result = JSON.parseObject(str, new TypeReference<Result>() {
        }.getType());
        assertNotNull(result);
    }

    public static class Result {
    }
}
