package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.NameFilter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7644 {
    @Test
    public void testNameFilterNullReturnPreservesMapKey() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("aaaa", "value");

        NameFilter nameFilter = (object, name, value) -> null;

        assertEquals("{\"aaaa\":\"value\"}", JSON.toJSONString(map, nameFilter));
    }

    @Test
    public void testNameFilterReturnValueRenamesMapKey() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("aaaa", "value");

        NameFilter nameFilter = (object, name, value) -> "renamed_" + name;

        assertEquals("{\"renamed_aaaa\":\"value\"}", JSON.toJSONString(map, nameFilter));
    }
}
