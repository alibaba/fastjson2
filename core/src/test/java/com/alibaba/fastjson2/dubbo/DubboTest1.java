package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest1 {
    @Test
    public void test() {
        assertEquals(
                String.class,
                JSON.parseObject(
                        "\"java.lang.String\"",
                        Class.class,
                        JSONReader.autoTypeFilter(String.class)
                )
        );
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.type = String.class;

        byte[] bytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(bytes, Bean.class, JSONReader.autoTypeFilter(String.class));
        assertEquals(bean.type, bean1.type);
    }

    public static class Bean {
        public Class type;
    }
}
