package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.ZoneId1;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZoneIdTest {
    @Test
    public void test_local() {
        ZoneId1 vo = new ZoneId1();
        vo.setV0000(ZoneId.of("Asia/Shanghai"));

        String str = JSON.toJSONString(vo);
        assertEquals("{\"v0000\":\"Asia/Shanghai\"}", str);

        ZoneId1 v2 = JSON.parseObject(str, ZoneId1.class);
        assertEquals(vo.getV0000(), v2.getV0000());
    }

    @Test
    public void test_local_jsonb() {
        ZoneId1 vo = new ZoneId1();
        vo.setV0000(ZoneId.of("Asia/Shanghai"));

        byte[] jsonbBytes = JSONB.toBytes(vo);
        ZoneId1 v2 = JSONB.parseObject(jsonbBytes, ZoneId1.class);
        assertEquals(vo.getV0000(), v2.getV0000());
    }
}
