package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.BeanContext;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1709 {
    @Test
    public void test() throws Exception {
        JSON.toJSONString(error(), new Myfilter());
    }

    public static class Myfilter
            implements ContextValueFilter {
        @Override
        public Object process(BeanContext context, Object object, String name, Object value) {
            assertNotNull(context.getField());
            assertNotNull(context.getMethod(), context.getField().toString());
            return value;
        }
    }

    public Exception error() {
        Exception error = null;
        try {
            int i = 5 / 0;
        } catch (Exception e) {
            error = e;
        }
        return error;
    }
}
