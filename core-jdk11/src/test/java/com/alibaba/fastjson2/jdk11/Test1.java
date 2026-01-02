package com.alibaba.fastjson2.jdk11;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test1 {
    private static class Bean {
        private int id;
        private String name;
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001;
        bean.name = "DataWorks";

        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        Bean bean1 = JSON.parseObject(str, Bean.class, JSONReader.Feature.FieldBased);

        assertEquals(bean.id, bean1.id);
        assertEquals(bean.name, bean1.name);
    }
}
