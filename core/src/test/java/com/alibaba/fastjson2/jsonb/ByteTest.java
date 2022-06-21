package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteTest {
    @Test
    public void test() {
        Byte value = 101;
        byte[] bytes = JSONB.toBytes(value, JSONWriter.Feature.WriteClassName);

        assertEquals(value, JSONB.parse(bytes));
        assertEquals(value, JSONB.parseObject(bytes, Object.class));

        assertEquals(
                Byte.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(Byte.class)
                        .getObjectClass()
        );
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.value = (byte) 101;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        assertEquals(bean.value, ((Bean) JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)).value);
        assertEquals(bean.value, ((Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType)).value);
    }

    public static class Bean {
        public Number value;
    }
}
