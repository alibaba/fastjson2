package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CurrencyTest5 {
    Locale locale;
    @BeforeEach
    public void setUp() throws Exception {
        locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(locale);
    }

    @Test
    public void test_0() throws Exception {
        SerializeConfig config = new SerializeConfig();
        config.put(Currency.class, config.createJavaBeanSerializer(Currency.class));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", Currency.getInstance("CNY"));

        String text = JSON.toJSONString(jsonObject, config);
        System.out.println(text);
        String str1 = "{\"value\":{\"currencyCode\":\"CNY\",\"displayName\":\"Chinese Yuan\",\"symbol\":\"CNY\"}}";
        String str2 = "{\"value\":{\"currencyCode\":\"CNY\",\"displayName\":\"人民币\",\"symbol\":\"￥\"}}";
        String str3 = "{\"value\":{\"currencyCode\":\"CNY\",\"displayName\":\"Chinese Yuan\",\"numericCodeAsString\":\"156\",\"symbol\":\"CN¥\"}}";
        String str4 = "{\"value\":{\"currencyCode\":\"CNY\",\"displayName\":\"人民币\",\"numericCodeAsString\":\"156\",\"symbol\":\"￥\"}}";
        assertTrue(text.equals(str1)
                || text.equals(str2)
                || text.equals(str3)
                || text.equals(str4));

        Currency currency = JSON.parseObject(text, VO.class).value;

        assertSame(Currency.getInstance("CNY"), currency);
    }

    public static class VO {
        public Currency value;
    }
}
