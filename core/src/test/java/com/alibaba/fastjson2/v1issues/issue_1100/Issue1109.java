package com.alibaba.fastjson2.v1issues.issue_1100;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * Created by wenshao on 28/03/2017.
 */
public class Issue1109 {
    @Test
    public void test_for_issue() throws Exception {
        Pair<String, String> data = Pair.of("key", "\"the\"content");
//        assertEquals("{\"key\":\"\\\"the\\\"content\"}", JSON.toJSONString(data));
    }
}
