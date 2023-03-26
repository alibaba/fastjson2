package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue1287 {
    @Test
    public void test() {
        assertFalse(
                JSON.isValid("033EYV000oYgHP1Rkw100g16Id0EYV0o")
        );
    }
}
