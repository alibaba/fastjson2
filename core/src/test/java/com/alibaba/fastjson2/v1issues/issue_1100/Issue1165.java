package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 27/04/2017.
 */
public class Issue1165 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = new Model();
        model.__v = 3;

        String json = JSON.toJSONString(model);
        assertEquals("{\"__v\":3}", json);
    }

    public static class Model {
        public Number __v;
    }
}
