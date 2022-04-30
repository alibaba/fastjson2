package com.alibaba.fastjson2.v1issues.issue_2100;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2182 {
    @Test
    public void test_for_issue() throws Exception {
        Multimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("admin", "admin.create");
        multimap.put("admin", "admin.update");
        multimap.put("admin", "admin.delete");
        multimap.put("user", "user.create");
        multimap.put("user", "user.delete");

        String json = JSON.toJSONString(multimap);
        assertEquals("{\"admin\":[\"admin.create\",\"admin.update\",\"admin.delete\"],\"user\":[\"user.create\",\"user.delete\"]}", json);

        ArrayListMultimap multimap1 = JSON.parseObject(json, ArrayListMultimap.class);

        assertEquals(multimap.size(), multimap1.size());
        assertEquals(json, JSON.toJSONString(multimap1));
    }
}
