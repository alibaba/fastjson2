package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSpecial_0 {
    @Test
    public void test_special() {
        Map<String, Object> vo = new HashMap<String, Object>();

        vo.put("a.b", 123);

        assertEquals((Integer) vo.get("a.b"), (Integer) JSONPath.eval(vo, "a\\.b"));
    }
}
