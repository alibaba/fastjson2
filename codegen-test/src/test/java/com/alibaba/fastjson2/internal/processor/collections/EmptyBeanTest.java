package com.alibaba.fastjson2.internal.processor.collections;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmptyBeanTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{}", JSON.toJSONString(bean));
    }

    @JSONCompiled
    public static class Bean {
    }
}
