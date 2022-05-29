package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {
    @Test
    public void test_exception() throws Exception {
        IllegalStateException ex = new IllegalStateException();
        String str = JSON.toJSONString(ex);
        System.out.println(str);

        Object jsonObject = JSON.parseObject(str, Object.class);
        assertTrue(jsonObject instanceof Map);

//        Throwable obj = JSON.parseObject(str, Throwable.class);
        Throwable e = new Throwable();
        String str1 = JSON.toJSONString(e);
        JSON.parseObject(str1, Throwable.class);

        Throwable throwable = JSON.parseObject(str, Throwable.class);
        assertEquals(Throwable.class, throwable.getClass());

        IllegalStateException error = (IllegalStateException)
                JSON.parseObject(str, Throwable.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(error);

        IllegalStateException error2 = JSON.parseObject(str, IllegalStateException.class);
        assertNotNull(error2);

        Throwable throwable2 = JSON.parseObject(str, Throwable.class);
        assertEquals(Throwable.class, throwable2.getClass());
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
