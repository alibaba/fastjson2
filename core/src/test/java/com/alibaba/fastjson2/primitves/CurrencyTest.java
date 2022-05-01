package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest {
    @Test
    public void test_local() {
        VO vo = new VO();
        vo.value = Currency.getInstance("CNY");

        String str = JSON.toJSONString(vo);
        assertEquals("{\"value\":\"CNY\"}", str);

        VO v2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.value, v2.value);
    }

    @Test
    public void test_local_jsonb() {
        VO vo = new VO();
        vo.value = Currency.getInstance("CNY");

        byte[] jsonbBytes = JSONB.toBytes(vo);
        VO v2 = JSONB.parseObject(jsonbBytes, VO.class);
        assertEquals(vo.value, v2.value);
    }

    public static class VO {
        public Currency value;
    }
}
