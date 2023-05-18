package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1494 {
    @Test
    public void test() {
        JSONWriter.Feature[] FASTJSON_DEFAULT_WRITER_FEATURES = {
                JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNullBooleanAsFalse, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteEnumsUsingName, JSONWriter.Feature.WritePairAsJavaBean, JSONWriter.Feature.IgnoreErrorGetter, JSONWriter.Feature.WriteBigDecimalAsPlain, JSONWriter.Feature.LargeObject
        };

        Map<String, Object> map = new HashMap<>();
        map.put("tag", "TAG1.cpu");
        map.put("qos", 0);
        map.put("time", 1684394501624L);
        map.put("value", new BigDecimal(String.format("%.2e", 3.74e4)));

        String expected = "{\"qos\":0,\"tag\":\"TAG1.cpu\",\"time\":1684394501624,\"value\":37400}";
        assertEquals(expected, JSON.toJSONString(map, FASTJSON_DEFAULT_WRITER_FEATURES));
        assertEquals(expected, new String(JSON.toJSONBytes(map, FASTJSON_DEFAULT_WRITER_FEATURES)));
    }
}
