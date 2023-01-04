package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1030 {
    @Test
    public void test() {
        Map<String, String> newParams = new HashMap<>();
        newParams.put("pageSize", "10");
        newParams.put("q", "");
        newParams.put("pageIndex", "0");
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("_t");
        String params = JSON.toJSONString(newParams, filter, JSONWriter.Feature.MapSortField);
        assertEquals("{\"pageIndex\":\"0\",\"pageSize\":\"10\",\"q\":\"\"}", params);
    }
}
