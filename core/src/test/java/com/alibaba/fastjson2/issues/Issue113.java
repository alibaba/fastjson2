package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue113 {
    @Test
    public void test() {
        String testJson2 = "{\"result\":[{\"puid\":\"21025318\"},{\"puid\":\"21482682\"},{\"puid\":\"21025345\"}],\"state\":0}";
        JSONArray result2 = (JSONArray) JSONPath.extract(testJson2, "$.result[0,2].puid");
        assertNotNull(result2);
        assertEquals("[\"21025318\",\"21025345\"]", result2.toJSONString());

        Object result3 = JSONPath.eval(testJson2, "$.result[0,2].puid");
        assertEquals("[\"21025318\",\"21025345\"]", result3.toString());
    }
}
