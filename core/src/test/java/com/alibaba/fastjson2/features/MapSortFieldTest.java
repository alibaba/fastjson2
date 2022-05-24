package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapSortFieldTest {
    @Test
    public void test() {
        assertEquals("{\"v2\":2,\"v1\":1}",
                JSON.toJSONString(
                        JSONObject.of("v2", 2, "v1", 1))
        );
        assertEquals("{\"v1\":1,\"v2\":2}",
                JSON.toJSONString(
                        JSONObject.of("v2", 2, "v1", 1),
                        JSONWriter.Feature.MapSortField)
        );

        LinkedHashMap map = new LinkedHashMap();
        map.put("v2", 2);
        map.put("v1", 1);
        assertEquals("{\"v2\":2,\"v1\":1}", JSON.toJSONString(map));
        assertEquals("{\"v2\":2,\"v1\":1}", JSON.toJSONString(map, JSONWriter.Feature.MapSortField));

        HashMap map2 = new HashMap();
        map2.put("v2", 2);
        map2.put("v1", 1);
        assertEquals("{\"v1\":1,\"v2\":2}", JSON.toJSONString(map2));
        assertEquals("{\"v1\":1,\"v2\":2}", JSON.toJSONString(map2, JSONWriter.Feature.MapSortField));
    }
}
