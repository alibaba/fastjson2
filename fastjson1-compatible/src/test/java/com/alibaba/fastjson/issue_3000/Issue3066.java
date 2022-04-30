package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3066 {
    @Test
    public void test_for_jsonpath() {
        String str = "{ 'id' : 0, 'items' : [ {'name': 'apple', 'price' : 30 }, {'name': 'pear', 'price' : 40 } ] }";
        JSONObject root = JSON.parseObject(str);
        Object max = JSONPath.eval(root, "$.items[*].price.max()");
        assertEquals(40, max);

        Object min = JSONPath.eval(root, "$.items[*].price.min()");
        assertEquals(30, min);

        Object count = JSONPath.eval(root, "$.items[*].price.size()");
        assertEquals(2, count);
    }
}
