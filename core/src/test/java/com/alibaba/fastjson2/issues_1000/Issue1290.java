package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1290 {
    @Test
    public void test() {
        IntFunction function = (int i) -> Integer.valueOf(i);
        Bean bean = new Bean();
        bean.function = function;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"function\":{}}", str);
    }

    public static class Bean {
        public Object function;
    }
}
