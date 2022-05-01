package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptinalIntTest {
    @Test
    public void test_0() {
        String str = "{\"value\":123}";
        VO vo = JSON.parseObject(str, VO.class);
        assertEquals(123, vo.value.getAsInt());
        String str2 = JSON.toJSONString(vo);
        assertEquals(str, str2);
    }

    @Test
    public void test_0_jsonb() {
        byte[] bytes = JSONB.toBytes(Collections.singletonMap("value", 123));
        VO vo = JSONB.parseObject(bytes, VO.class);
        assertEquals(123, vo.value.getAsInt());
    }

    @Test
    public void test_empty() {
        String str = "{\"value\":null}";
        VO vo = JSON.parseObject(str, VO.class);
        assertEquals(false, vo.value.isPresent());
        String str2 = JSON.toJSONString(vo);
        assertEquals(str, str2);
    }

    @Test
    public void test_enpty_jsonb() {
        byte[] bytes = JSONB.toBytes(Collections.singletonMap("value", null), JSONWriter.Feature.WriteNulls);
        VO vo = JSONB.parseObject(bytes, VO.class);
        assertEquals(false, vo.value.isPresent());
    }

    public static class VO {
        public OptionalInt value;
    }
}
