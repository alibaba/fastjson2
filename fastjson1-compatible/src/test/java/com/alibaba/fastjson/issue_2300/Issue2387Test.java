package com.alibaba.fastjson.issue_2300;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
import com.alibaba.fastjson.JSONPath;

public class Issue2387Test {
    @Test
    public void test_for_issue() throws Exception {
        String data = "{\"userName\":\"testname\"}";
        Object userName1 = JSONPath.eval(data, "$.userName");
        assertEquals(userName1, "testname");
    }
    
    @Test
    public void test_not_matching() {
        String data = "{\"userName\":\"testname\"}";
        Object userName1 = JSONPath.eval(data, "$.userName");
        assertNotEquals(userName1, "testname1");
    }

    @Test
    public void test_null() throws Exception {
        String data = "{\"userName\":null}";
        Object userName1 = JSONPath.eval(data, "$.userName");
        assertNull(userName1);
    }
}
