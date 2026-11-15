package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7775 {
    @Test
    public void extractRootArray() {
        String json = "[{\"nationTwoAbbr\":\"CN\"},{\"nationTwoAbbr\":\"IN\"}]";
        Object result = JSONPath.extract(json, "$.nationTwoAbbr");
        assertEquals(JSON.parseArray("[\"CN\",\"IN\"]"), result);
        assertTrue(result instanceof JSONArray);
    }
}
