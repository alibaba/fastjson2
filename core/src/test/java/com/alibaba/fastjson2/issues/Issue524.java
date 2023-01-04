package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue524 {
    @Test
    public void test() {
        String jsonStr = "{\"data\":[[{\"value\":1958,\"hb\":-0.03},{\"value\":0.28,\"hb\":-0.04}]]}";

        JSONPath path = JSONPath.of("$.data[*][1][?(@.value == 0.28)]");
        JSONReader parser = JSONReader.of(jsonStr);
        Object fastjsonRes = path.extract(parser);
        assertEquals("[{\"value\":0.28,\"hb\":-0.04}]", JSON.toJSONString(fastjsonRes));
    }
}
