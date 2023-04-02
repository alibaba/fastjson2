package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1254 {
    @Test
    public void test() {
        String str = "{\"Aa\": 336, \"A_a\": 210, \"aA\": 5505, \"a_A\": 336}";
        JSONReader.Context context = JSONFactory.createReadContext(TreeMap::new);
        TreeMap treeMap = (TreeMap) JSON.parse(str, context);
        assertEquals(336, treeMap.get("Aa"));
    }
}
