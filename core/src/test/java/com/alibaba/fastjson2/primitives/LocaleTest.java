package com.alibaba.fastjson2.primitives;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("primitives")
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

    @Test
    public void test_script() {
        VO vo = new VO();
        vo.locale = Locale.forLanguageTag("zh-Hant-HK");

        String str = JSON.toJSONString(vo);
        assertEquals("{\"locale\":\"zh_HK_#Hant\"}", str);

        VO v2 = JSON.parseObject(str, VO.class);
        assertEquals(vo.locale, v2.locale);
    }

    @Test
    public void test_script_jsonb() {
        VO vo = new VO();
        vo.locale = Locale.forLanguageTag("ja-Jpan-JP-u-ca-japanese");

        byte[] jsonbBytes = JSONB.toBytes(vo);
        VO v2 = JSONB.parseObject(jsonbBytes, VO.class);
        assertEquals(vo.locale, v2.locale);
    }

    @Test
    public void test_extension() {
        Locale locale = Locale.forLanguageTag("en-US-u-ca-japanese");
        assertEquals(locale, JSON.parseObject(JSON.toJSONString(locale), Locale.class));
    }

    @Test
    public void test_root() {
        assertEquals("\"\"", JSON.toJSONString(Locale.ROOT));
        assertEquals(Locale.ROOT, JSON.parseObject("\"\"", Locale.class));
    }

    public static class VO {
        public Locale locale;
    }
}
