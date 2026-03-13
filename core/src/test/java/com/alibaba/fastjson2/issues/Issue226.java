package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("regression")
public class Issue226 {
    @Test
    public void test() {
        JSON.parseObject("{\"geo\":{\"latitude\":5.73E-32,\"longitude\":2.0E-322}}");
    }
}
