package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.function.IntFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1290 {
    @Test
    public void test() {
        IntFunction function = Integer::valueOf;
        Bean bean = new Bean();
        bean.function = function;
        String str = JSON.toJSONString(bean);
        assertEquals("{\"function\":{}}", str);
    }

    public static class Bean {
        public Object function;
    }

    @Test
    public void test1() {
        IntFunction function = (int i) -> Integer.valueOf(i);
        Bean1 bean = new Bean1();
        bean.functions = new Object[] {function};
        String str = JSON.toJSONString(bean);
        assertEquals("{\"functions\":[{}]}", str);
    }

    public static class Bean1 {
        public Object[] functions;
    }
}
