package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocaleTest {
    @Test
    public void test_local() {
        VO vo = new VO();
        vo.locale = Locale.CHINA;

        String str = JSON.toJSONString(vo);
        assertEquals("{\"locale\":\"zh_CN\"}", str);

        VO v2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.locale, v2.locale);
    }

    @Test
    public void test_local_jsonb() {
        VO vo = new VO();
        vo.locale = Locale.CHINA;

        byte[] jsonbBytes = JSONB.toBytes(vo);
        VO v2 = JSONB.parseObject(jsonbBytes, VO.class);
        assertEquals(vo.locale, v2.locale);
    }

    public static class VO {
        public Locale locale;
    }
}
