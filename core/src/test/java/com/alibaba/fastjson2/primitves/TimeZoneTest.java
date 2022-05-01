package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeZoneTest {
    @Test
    public void test_local() {
        VO vo = new VO();
        vo.value = TimeZone.getTimeZone("Asia/Shanghai");

        String str = JSON.toJSONString(vo);
        assertEquals("{\"value\":\"Asia/Shanghai\"}", str);

        VO v2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.value, v2.value);
    }

    @Test
    public void test_local_jsonb() {
        VO vo = new VO();
        vo.value = TimeZone.getTimeZone("Asia/Shanghai");

        byte[] jsonbBytes = JSONB.toBytes(vo);
        VO v2 = JSONB.parseObject(jsonbBytes, VO.class);
        assertEquals(vo.value, v2.value);
    }

    public static class VO {
        public TimeZone value;
    }
}
