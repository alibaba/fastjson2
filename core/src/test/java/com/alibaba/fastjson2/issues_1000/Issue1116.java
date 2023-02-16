package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class Issue1116 {
    @Test
    public void test() {
        String base64Str = "ZWMtZnVuZDpwYXlvdXQtYmlsbC11cHNlcnRlZA==";
        JSONObject object = JSONObject.of("value", base64Str);
        Bean bean = object.toJavaObject(Bean.class, JSONReader.Feature.Base64StringAsByteArray);
        assertArrayEquals(Base64.getDecoder().decode(base64Str), bean.value);
    }

    public static class Bean {
        public byte[] value;
    }
}
