package com.alibaba.json.bvt.issue_2100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2165 {
    @Test
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("9295260120", Integer.class);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
        assertEquals("parseInt error", error.getMessage());
    }

    @Test
    public void test_for_issue_1() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"value\":9295260120}", Model.class);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
        assertEquals("parseInt error, field : value", error.getMessage());
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("[9295260120]", Model.class, Feature.SupportArrayToBean);
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
        assertEquals("parseInt error : 9295260120", error.getMessage());
    }

    public static class Model {
        public int value;
    }
}
