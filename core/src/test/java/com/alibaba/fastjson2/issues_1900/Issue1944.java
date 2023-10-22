package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue1944 {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("id", 123, "name", "xxx");
        String str = jsonObject.toJSONString();
        assertEquals(
                jsonObject.getIntValue("id"),
                JSON.parseObject(str, Bean.class).id);
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(str, Bean.class, JSONReader.Feature.ErrorOnUnknownProperties)
        );

        assertEquals(
                jsonObject.getIntValue("id"),
                jsonObject.toJavaObject(Bean.class).id
        );
        assertThrows(
                JSONException.class,
                () -> jsonObject.toJavaObject(Bean.class, JSONReader.Feature.ErrorOnUnknownProperties)
        );
    }

    public static class Bean {
        public int id;
    }
}
