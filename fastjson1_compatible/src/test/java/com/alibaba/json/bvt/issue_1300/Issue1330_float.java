package com.alibaba.json.bvt.issue_1300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import junit.framework.TestCase;

/**
 * Created by wenshao on 30/07/2017.
 */
public class Issue1330_float extends TestCase {
    public void test_for_issue() throws Exception {
        Exception error = null;
        try {
            JSON.parseObject("{\"value\":\"ABC\"}", Model.class);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
//        assertTrue(error.getMessage().indexOf("parseInt error") != -1);
    }

    public void test_for_issue_1() throws Exception {
        Model model = JSON.parseObject("{\"value\":[]}", Model.class);
        assertEquals(0F, model.value);
    }

    public void test_for_issue_2() throws Exception {
        Model model = JSON.parseObject("{\"value\":{}}", Model.class);
        assertEquals(0F, model.value);
    }

    public static class Model {
        public float value;
    }
}
