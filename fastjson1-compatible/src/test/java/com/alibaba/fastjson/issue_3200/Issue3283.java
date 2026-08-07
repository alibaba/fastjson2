package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3283 {
    @Test
    public void test_for_issue() {
        VO v = new VO();
        v.v0 = 1001L;
        v.v1 = 101;

        String str = JSON.toJSONString(v, SerializerFeature.WriteNonStringValueAsString);
        assertEquals("{\"v0\":\"1001\",\"v1\":\"101\"}", str);

        JSONObject object = JSON.parseObject(str);
        assertEquals("1001", object.get("v0"));
        assertEquals("101", object.get("v1"));
    }

    public void test_for_issue_1() {
        VO v = new VO();
        v.v0 = 19007199254740991L;

        String str = JSON.toJSONString(v, SerializerFeature.BrowserCompatible);
        assertEquals("{\"v0\":\"19007199254740991\"}", str);
    }

    public static class VO {
        public Long v0;
        public Integer v1;
    }
}
