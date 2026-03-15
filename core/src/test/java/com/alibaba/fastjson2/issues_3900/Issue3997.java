package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3997 {
    static final String JSON_STR = "{\n"
            + "  \"riskRequests\": [\n"
            + "    {\"requestRiskType\": \"05\", \"customerType\": \"01\", \"customerName\": \"Alice\"},\n"
            + "    {\"requestRiskType\": \"05\", \"customerType\": \"02\", \"customerName\": \"Bob\"},\n"
            + "    {\"requestRiskType\": \"06\", \"customerType\": \"04\", \"customerName\": \"Charlie\"}\n"
            + "  ]\n"
            + "}";

    @Test
    public void testInFilterAfterEquality() {
        // This was the failing case: 'in' filter after '==' filter with '&&'
        Object result = JSONPath.eval(JSON_STR,
                "$.riskRequests[?(@.requestRiskType=='05' && @.customerType in ('01','04'))].customerName");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals("Alice", arr.get(0));
    }

    @Test
    public void testInFilterBeforeEquality() {
        // This was the working case: 'in' filter before '==' filter
        Object result = JSONPath.eval(JSON_STR,
                "$.riskRequests[?(@.customerType in ('01','04') && @.requestRiskType=='05')].customerName");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals("Alice", arr.get(0));
    }

    @Test
    public void testInFilterAlone() {
        Object result = JSONPath.eval(JSON_STR,
                "$.riskRequests[?(@.customerType in ('01','04'))].customerName");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(2, arr.size());
    }

    @Test
    public void testInFilterWithOr() {
        Object result = JSONPath.eval(JSON_STR,
                "$.riskRequests[?(@.requestRiskType=='06' || @.customerType in ('01'))].customerName");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(2, arr.size());
    }

    @Test
    public void testNotInAfterEquality() {
        Object result = JSONPath.eval(JSON_STR,
                "$.riskRequests[?(@.requestRiskType=='05' && @.customerType not in ('01'))].customerName");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals("Bob", arr.get(0));
    }

    @Test
    public void testNumericInAfterEquality() {
        String json = "{\"items\":[{\"type\":\"a\",\"code\":1},{\"type\":\"a\",\"code\":2},{\"type\":\"b\",\"code\":1}]}";
        Object result = JSONPath.eval(json,
                "$.items[?(@.type=='a' && @.code in (1))].code");
        assertNotNull(result);
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals(1, arr.get(0));
    }
}
