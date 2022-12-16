package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest1 {
    @Test
    public void test() {
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(String.class);
        assertEquals(
                String.class,
                JSON.parseObject(
                        "\"java.lang.String\"",
                        Class.class,
                        filter
                )
        );
        assertEquals(
                String.class,
                JSON.parseObject(
                        "\"java.lang.String\"",
                        Class.class,
                        filter
                )
        );
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.type = String.class;

        byte[] bytes = JSONB.toBytes(bean);
        JSONBDump.dump(bytes);

        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(String.class);
        Bean bean1 = JSONB.parseObject(bytes, Bean.class, filter);
        assertEquals(bean.type, bean1.type);

        Bean bean2 = JSONB.parseObject(bytes, Bean.class, filter);
        assertEquals(bean.type, bean2.type);
    }

    public static class Bean {
        public Class type;
    }
}
