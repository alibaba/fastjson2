package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ListReferenceReviewTest {
    @Test
    public void parentReferencePointsToList() {
        List<Object> list = JSON.parseObject("[{\"$ref\":\"..\"}]", new TypeReference<List<Object>>() { });
        assertSame(list, list.get(0));
    }

    @Test
    public void unresolvedReferencePreservesFollowingElement() {
        List<Map<String, Integer>> list = JSON.parseObject(
                "[{\"a\":1},{\"$ref\":\"$[0]\"},{\"b\":2}]",
                new TypeReference<List<Map<String, Integer>>>() { });
        assertEquals(3, list.size());
        assertSame(list.get(0), list.get(1));
        assertEquals(2, list.get(2).get("b"));
    }
}
