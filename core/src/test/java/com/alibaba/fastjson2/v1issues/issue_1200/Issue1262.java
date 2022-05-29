package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wenshao on 15/06/2017.
 */
public class Issue1262 {
    @Test
    public void test_for_issue() throws Exception {
        Model model = JSON.parseObject("{\"chatterMap\":{}}", Model.class);
    }

    public static class Model {
        public Map<String, Chatter> chatterMap = new ConcurrentHashMap<String, Chatter>();
    }

    public static class Chatter {
    }
}
