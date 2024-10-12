package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2672 {
    @Test
    public void test() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("selfRef", map);
        map.put("k", "v");
        assertEquals("{\"selfRef\":{\"$ref\":\"..\"},\"k\":\"v\"}",
                JSON.toJSONString(map, JSONWriter.Feature.ReferenceDetection));

        PropertyPreFilter propertyJsonFilter = (serializer, source, property) -> true;
        assertEquals("{\"selfRef\":{\"$ref\":\"$\"},\"k\":\"v\"}",
                JSON.toJSONString(map, propertyJsonFilter, JSONWriter.Feature.ReferenceDetection));
    }
}
