package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1919 {
    @Test
    public void test() {
        StringBuilder buf = new StringBuilder("{\"precedeId\":1.");
        for (int i = 0; i < 2000; i++) {
            buf.append('0');
        }
        String str = buf.append("}").toString();
        JSONObject jsonObject = JSON.parseObject(str);
        assertEquals(str, jsonObject.toJSONString(JSONWriter.Feature.WriteBigDecimalAsPlain));
    }

    @Test
    public void testError() {
        StringBuilder buf = new StringBuilder("{\"precedeId\":1.");
        for (int i = 0; i < 10000; i++) {
            buf.append('0');
        }
        String str = buf.append("}").toString();
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str));
    }
}
