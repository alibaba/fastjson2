package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 13/04/2017.
 */
public class Issue1144 {
    @Test
    public void test_issue_1144() throws Exception {
        Model model = new Model();
        String json = JSON.toJSONString(model);
        assertEquals("{\"f0\":0,\"f1\":0,\"f2\":0}", json);
    }

    public static class Model {
        public int f2;
        public int f1;
        public int f0;
    }
}
