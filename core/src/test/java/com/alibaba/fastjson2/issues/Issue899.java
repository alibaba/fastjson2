package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue899 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        ClassLoader prevContextCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            {
                Bean o = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
                assertEquals(bean.id, o.id);
            }
            {
                Bean o = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
                assertEquals(bean.id, o.id);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(prevContextCL);
        }
    }

    public static class Bean {
        public int id;
    }
}
