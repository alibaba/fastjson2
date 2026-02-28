package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3997 {
    @Test
    public void testInFilterAfterOtherFilter() {
        JSONArray riskRequests = JSONArray.of(
                JSONObject.of("requestRiskType", "05", "customerType", "01", "customerName", "Alice"),
                JSONObject.of("requestRiskType", "05", "customerType", "04", "customerName", "Bob"),
                JSONObject.of("requestRiskType", "05", "customerType", "02", "customerName", "Carol"),
                JSONObject.of("requestRiskType", "06", "customerType", "01", "customerName", "Dave")
        );
        JSONObject root = JSONObject.of("riskRequests", riskRequests);

        Object result = JSONPath.of("$.riskRequests[?(@.requestRiskType=='05' && @.customerType in ('01','04'))].customerName")
                .eval(root);
        assertEquals("[\"Alice\",\"Bob\"]", JSON.toJSONString(result));
    }

    @Test
    public void testInFilterBeforeOtherFilter() {
        JSONArray riskRequests = JSONArray.of(
                JSONObject.of("requestRiskType", "05", "customerType", "01", "customerName", "Alice"),
                JSONObject.of("requestRiskType", "05", "customerType", "04", "customerName", "Bob"),
                JSONObject.of("requestRiskType", "05", "customerType", "02", "customerName", "Carol"),
                JSONObject.of("requestRiskType", "06", "customerType", "01", "customerName", "Dave")
        );
        JSONObject root = JSONObject.of("riskRequests", riskRequests);

        Object result = JSONPath.of("$.riskRequests[?(@.customerType in ('01','04') && @.requestRiskType=='05')].customerName")
                .eval(root);
        assertEquals("[\"Alice\",\"Bob\"]", JSON.toJSONString(result));
    }
}
