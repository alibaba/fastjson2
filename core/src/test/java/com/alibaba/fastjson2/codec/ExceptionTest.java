package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExceptionTest {
    @Test
    public void test_exception() throws Exception {
        IllegalStateException ex = new IllegalStateException();
        String str = JSON.toJSONString(ex);
        System.out.println(str);

        Object jsonObject = JSON.parseObject(str, Object.class);
        assertTrue(jsonObject instanceof Map);

        IllegalStateException error = (IllegalStateException)
                JSON.parseObject(str, Throwable.class);
        assertNotNull(error);
    }

    @Test
    public void test_exception_jsonb() throws Exception {
        IllegalStateException ex = new IllegalStateException();
        byte[] jsonbBytes = JSONB.toBytes(ex);

        JSONBDump.dump(jsonbBytes);

        Object jsonObject = JSONB.parseObject(jsonbBytes, Object.class);
        assertTrue(jsonObject instanceof Map);

        IllegalStateException error = (IllegalStateException)
                JSONB.parseObject(jsonbBytes, Throwable.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(error);
    }
}
