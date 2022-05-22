package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoTypeTest49 {
    @Test
    public void test_0() throws Exception {
        String className = Bean.class.getName();

        JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept(AutoTypeTest49.class.getName() + ".B");

        Bean bean = (Bean) JSON.parseObject("{\"@type\":\"" + className + "\"}", Object.class);
        assertNotNull(bean);

        Object o = JSON.parseObject("{\"@type\":\"" + A.class + "\"}", Object.class);
        assertTrue(o instanceof JSONObject);
    }

    public static class Bean {
    }

    public static class A {
    }
}
