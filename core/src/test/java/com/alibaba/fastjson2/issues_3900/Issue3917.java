package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3917 {
    @Test
    public void test() {
        String json = "{\"a\":1.4900000000000002396241300002543983538316751946695148944854736328125e-7}";
        A a = JSON.parseObject(json, A.class);

        JSONObject obj = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), JSONReader.Feature.UseBigDecimalForDoubles); //JSONReaderUTF8#readNumber0()
        assertEquals(obj.get("a"), a.a);

        obj = JSON.parseObject(json.toCharArray(), 0, json.toCharArray().length, JSONReader.Feature.UseBigDecimalForDoubles); //JSONReaderUTF16#readNumber0()
        assertEquals(obj.get("a"), a.a);
    }

    public static class A {
        public BigDecimal a;
    }
}
