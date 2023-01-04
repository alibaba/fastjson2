package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortTest {
    @Test
    public void test() {
        Short value = 101;
        byte[] bytes = JSONB.toBytes(value, JSONWriter.Feature.WriteClassName);

        assertEquals(value, JSONB.parse(bytes));
        assertEquals(value, JSONB.parseObject(bytes, Object.class));

        assertEquals(
                Short.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Short.class)
                        .getObjectClass()
        );
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.value = (short) 101;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        assertEquals(bean.value, ((Bean) JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)).value);
        assertEquals(bean.value, ((Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType)).value);
    }

    public static class Bean {
        public Number value;
    }
}
