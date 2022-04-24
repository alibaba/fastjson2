package com.alibaba.fastjson.basicType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimal_BrowserCompatible {
    @Test
    public void test_for_issue() throws Exception {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id1", new BigDecimal("-9223370018640066466"));
        map.put("id2", new BigDecimal("9223370018640066466"));
        map.put("id3", new BigDecimal("100"));
        assertEquals("{\"id1\":\"-9223370018640066466\",\"id2\":\"9223370018640066466\",\"id3\":100}",
                JSON.toJSONString(map, SerializerFeature.BrowserCompatible)
        );
    }
}
