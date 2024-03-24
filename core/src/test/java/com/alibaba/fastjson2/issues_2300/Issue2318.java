package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2318 {
    @Test
    public void test() {
        Map<Object, Object> m = new LinkedHashMap<>();
        m.put("abc", 1);
        m.put("123", 2);
        assertEquals("{\"abc\":1,\"123\":2}", JSON.toJSONString(m, JSONWriter.Feature.MapSortField));
        assertEquals("{\"123\":2,\"abc\":1}", JSON.toJSONString(m, JSONWriter.Feature.SortMapEntriesByKeys));

        Bean bean = new Bean();
        bean.map = m;

        assertEquals("{\"map\":{\"abc\":1,\"123\":2}}", JSON.toJSONString(bean, JSONWriter.Feature.MapSortField));
        assertEquals("{\"map\":{\"123\":2,\"abc\":1}}", JSON.toJSONString(bean, JSONWriter.Feature.SortMapEntriesByKeys));
    }

    public static class Bean {
        public Map map;
    }
}
