package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

public class Issue2086 {
    @Test
    public void test_for_issue() throws Exception {
        JSON.parseObject("{\"id\":123}", Model.class);
        JSON.toJSONString(new Model());
    }

    public static class Model {
        public void set() {
        }
    }
}
