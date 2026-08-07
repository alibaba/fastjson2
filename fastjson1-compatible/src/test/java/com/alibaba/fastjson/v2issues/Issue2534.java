package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2534 {
    @Test
    public void testExtract1Mutatedfj() {
        String raw = "[[{\"a\":1},{\"a\":2}],[{\"a\":3}]]";
        assertEquals("{\"a\":2}", ((JSONObject) JSONPath.extract(raw, "$[0][1]")).toJSONString());
    }
}
