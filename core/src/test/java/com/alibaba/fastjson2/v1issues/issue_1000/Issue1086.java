package com.alibaba.fastjson2.v1issues.issue_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by wenshao on 20/03/2017.
 */
public class Issue1086 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = JSON.parseObject("{\"flag\":1}", Model.class);
        assertTrue(model.flag);
    }

    public static class Model {
        public boolean flag;
    }
}
