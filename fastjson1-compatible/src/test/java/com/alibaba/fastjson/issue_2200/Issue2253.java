package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2253 {
    @Test
    public void test_for_issue() throws Exception {
        List<Map<String, Object>> result = new ArrayList();
        result.add(new LinkedHashMap());
        result.get(0).put("3", 3);
        result.get(0).put("2", 2);
        result.get(0).put("7", 7);

        assertEquals("[{\"3\":3,\"2\":2,\"7\":7}]", JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));

        result = JSON.parseObject(JSON.toJSONString(result, SerializerFeature.WriteMapNullValue), new TypeReference<List<Map<String, Object>>>() {
        }, Feature.OrderedField);

        assertEquals("[{\"3\":3,\"2\":2,\"7\":7}]", JSON.toJSONString(result, SerializerFeature.WriteMapNullValue));
    }
}
