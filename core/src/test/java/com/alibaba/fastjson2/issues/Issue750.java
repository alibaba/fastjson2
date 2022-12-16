package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue750 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 123;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean bean1 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.autoTypeFilter(Bean.class));
        assertEquals(bean.id, bean1.id);
    }

    public static class Bean {
        public int id;
    }
}
