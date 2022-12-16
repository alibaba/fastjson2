package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1018 {
    @Test
    public void test() {
        String path = "$.data.plan.targets[*].set_points[0]";
        assertNull(JSONPath.eval("", path));
        assertNull(JSONPath.eval("{}", path));
        assertNull(JSONPath.eval("{\"data\":{}}", path));
        assertNull(JSONPath.eval("{\"data\":{\"plan\":{}}}", path));

        assertEquals(
                "[]",
                JSON.toJSONString(JSONPath.extract("", path, JSONPath.Feature.AlwaysReturnList))
        );
        assertEquals(
                "[]",
                JSON.toJSONString(JSONPath.extract("{}", path, JSONPath.Feature.AlwaysReturnList))
        );
        assertEquals(
                "[]",
                JSON.toJSONString(JSONPath.extract("{\"data\":{}}", path, JSONPath.Feature.AlwaysReturnList))
        );
        assertEquals(
                "[]",
                JSON.toJSONString(
                        JSONPath.eval("{\"data\":{\"plan\":{\"targets\":[]}}}", path)
                )
        );
    }
}
