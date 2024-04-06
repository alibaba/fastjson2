package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1974 {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("username", "<script>test666");
        assertEquals(
                "{\"username\":\"\\u003Cscript\\u003Etest666\"}",
                jsonObject.toJSONString(JSONWriter.Feature.BrowserSecure));
    }
}
