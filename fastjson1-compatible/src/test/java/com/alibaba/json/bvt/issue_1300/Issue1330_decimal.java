package com.alibaba.json.bvt.issue_1300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 30/07/2017.
 */
public class Issue1330_decimal {
    @Test
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"value\":\"中ABC\"}", Model.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
//        assertTrue(error.getMessage().indexOf("parseDecimal error, field : value") != -1);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        Model model = JSON.parseObject("{\"value\":[]}", Model.class);
        assertNull(model.value);
    }

    @Test
    public void test_for_issue_2() throws Exception {
        Model model = JSON.parseObject("{\"value\":[]}", Model.class);
        assertNull(model.value);
    }

    public static class Model {
        public BigDecimal value;
    }
}
