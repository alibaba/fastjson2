package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest50 {
    @Test
    public void test0() {
        Bean bean = (Bean) JSON.parseObject("{\"@type\":\"AutoTypeTest50$Bean\",\"id\":123}", Object.class, new LocalAutoTypeBeforeHandler());
        assertEquals(123, bean.id);
    }

    public static class LocalAutoTypeBeforeHandler
            implements JSONReader.AutoTypeBeforeHandler {
        @Override
        public Class<?> apply(String typeName, Class<?> expectClass, long features) {
            if ("AutoTypeTest50$Bean".equals(typeName)) {
                return Bean.class;
            }

            return null;
        }
    }

    public static class Bean {
        public int id;
    }
}
