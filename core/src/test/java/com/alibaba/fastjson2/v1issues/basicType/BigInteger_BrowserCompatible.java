package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigInteger_BrowserCompatible {
    @Test
    public void test_for_issue() throws Exception {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id1", 9223370018640066466L);
        map.put("id2", new BigInteger("9223370018640066466"));
        assertEquals("{\"id1\":\"9223370018640066466\",\"id2\":\"9223370018640066466\"}",
                JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible)
        );
    }
}
