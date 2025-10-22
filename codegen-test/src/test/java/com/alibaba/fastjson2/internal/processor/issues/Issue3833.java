package com.alibaba.fastjson2.internal.processor.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONType;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3833 {
    @Test
    public void test() {
        String json = "{\"elements\":{\"7\":{\"id\":100,\"type\":\"datetime\",\"value\":\"0302054257\"}}}";

        MyDto fixedDto = JSON.parseObject(json, MyDto.class);
        assertNotNull(fixedDto.elements);
        assertTrue(fixedDto.elements.containsKey("7"));

        Element element = fixedDto.elements.get("7");
        assertEquals(100, element.id);
    }

    @JSONCompiled
    @JSONType
    public static class MyDto {
        public Map<String, Element> elements;
    }

    @JSONCompiled
    @Data
    public static class Element {
        public int id;
        public String type;
        public String value;
    }
}
