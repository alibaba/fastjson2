package com.alibaba.fastjson2.issues_2400;

import cn.hutool.core.map.CaseInsensitiveLinkedMap;
import cn.hutool.core.map.CaseInsensitiveMap;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2458 {
    @Test
    public void test() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("id", 123);
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.FieldBased));
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.WriteNullStringAsEmpty));
    }

    @Test
    public void test1() {
        CaseInsensitiveLinkedMap map = new CaseInsensitiveLinkedMap();
        map.put("id", 123);
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.FieldBased));
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.WriteNullStringAsEmpty));
    }

    @Test
    public void test2() {
        Map map = new org.apache.commons.collections.map.CaseInsensitiveMap();
        map.put("id", 123);
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.FieldBased));
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(map, JSONWriter.Feature.WriteNullStringAsEmpty));
    }
}
