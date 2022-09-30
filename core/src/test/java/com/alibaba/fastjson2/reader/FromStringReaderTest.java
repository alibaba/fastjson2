package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FromStringReaderTest {
    ObjectReaderProvider provider;

    @BeforeEach
    public void setUp() {
        provider = JSONFactory.getDefaultObjectReaderProvider();
        provider.register(Bean.class, ObjectReaders.ofString(Bean::new));
    }

    public void tearDown() {
        provider.unregisterObjectReader(Bean.class);
    }

    @Test
    public void test() {
        Bean bean = JSON.parseObject("123", Bean.class);
        assertEquals(123, bean.code);
    }

    @Test
    public void testJSONB() {
        Bean bean = JSONB.parseObject(JSONB.toBytes(123), Bean.class);
        assertEquals(123, bean.code);
    }

    public static class Bean {
        final int code;
        public Bean(String str) {
            code = Integer.parseInt(str);
        }
    }
}
