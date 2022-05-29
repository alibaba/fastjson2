package com.alibaba.fastjson2.v1issues.issue_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3679 {
    private static final String list_map_st_obj = "[{\"123\":951753},{\"456\":\"second\"},{\"789\":777},{\"999\":[1,2,3]}]";

    @Test
    public void test_for_issue3679() {
        List<Object> temp = JSON.parseArray(list_map_st_obj, new Type[]{
                new TypeReference<Map<String, Integer>>() {
                }.getType(),
                new TypeReference<Map<String, String>>() {
                }.getType(),
                new TypeReference<Map<String, Integer>>() {
                }.getType(),
                new TypeReference<Map<String, int[]>>() {
                }.getType(),
        });
        assertEquals(list_map_st_obj, JSON.toJSONString(temp));
    }
}
