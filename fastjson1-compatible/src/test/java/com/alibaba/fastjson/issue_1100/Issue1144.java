package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import org.junit.jupiter.api.Test;

/**
 * Created by wenshao on 13/04/2017.
 */
public class Issue1144 {
    @Test
    public void test_issue_1144() throws Exception {
        Model model = new Model();
        String json = JSON.toJSONString(model);
        System.out.println(json);
    }

    @JSONType(alphabetic = false)
    public static class Model {
        public int f2;
        public int f1;
        public int f0;
    }
}
