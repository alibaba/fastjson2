package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue699 {
    @Test
    public void test() {
        JSONArray array = JSON.parseArray("[\"a\",\"b\",\"c\"]");
        List<String> list = array.toJavaList(String.class);
        assertEquals(array.get(0), list.get(0));
    }
}
