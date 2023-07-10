package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1627 {
    @Test
    public void test() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("kotlin.collections.EmptyList");
        jsonWriter.startArray(0);
        byte[] bytes = jsonWriter.getBytes();
        assertEquals(
                "java.util.ArrayList",
                JSONB.parseObject(bytes, List.class).getClass().getName()
        );
        assertEquals(
                "kotlin.collections.EmptyList",
                JSONB.parseObject(bytes, List.class, JSONReader.Feature.SupportAutoType).getClass().getName()
        );
    }

    @Test
    public void testSet() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeTypeName("kotlin.collections.EmptySet");
        jsonWriter.startArray(0);
        byte[] bytes = jsonWriter.getBytes();
        assertEquals(
                "java.util.HashSet",
                JSONB.parseObject(bytes, Set.class).getClass().getName()
        );
        assertEquals(
                "kotlin.collections.EmptySet",
                JSONB.parseObject(bytes, Set.class, JSONReader.Feature.SupportAutoType).getClass().getName()
        );
    }
}
