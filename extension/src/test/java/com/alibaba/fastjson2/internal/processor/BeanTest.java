package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

public class BeanTest {
    @Test
    public void test() {
        String str = "{}";
        JSON.parseObject(str, Bean.class);
    }
}
