package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 28/03/2017.
 */
public class Issue1109 {
    @Test
    public void test_for_issue() throws Exception {
        Pair<String, String> data = Pair.of("key", "\"the\"content");
        assertEquals("{\"key\":\"\\\"the\\\"content\"}", JSON.toJSONString(data));
    }
}
