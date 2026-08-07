package com.alibaba.fastjson.issue_1000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 11/06/2017.
 */
public class Issue1083 {
    @Test
    public void test_for_issue() throws Exception {
        Map map = new HashMap();
        map.put("userId", 456);
        String json = JSON.toJSONString(map, SerializerFeature.WriteNonStringValueAsString);
        assertEquals("{\"userId\":\"456\"}", json);
    }
}
