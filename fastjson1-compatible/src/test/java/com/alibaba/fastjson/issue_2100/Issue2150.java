package com.alibaba.fastjson.issue_2100;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2150 {
    @Test
    public void test_for_issue() throws Exception {
        int[][][] arr = new int[100][100][100];
        JSONArray jsonObj = (JSONArray) JSONArray.toJSON(arr);
        assertNotNull(jsonObj);
        assertNotNull(jsonObj.getJSONArray(0));
    }
}
