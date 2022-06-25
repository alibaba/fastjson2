package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Author ：Nanqi
 * @Date ：Created in 22:29 2020/7/15
 */
public class Issue3347 {
    @Test
    public void test_for_issue() throws Exception {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "hello");
        String mapJSONString = JSON.toJSONString(map);
        Map mapValues = JSONObject.parseObject(mapJSONString, Map.class);
        Object mapKey = mapValues.keySet().iterator().next();
        assertTrue(mapKey instanceof Integer);
    }
}
