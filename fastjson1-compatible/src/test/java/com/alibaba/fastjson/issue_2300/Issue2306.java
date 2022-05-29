package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2306 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject object = new JSONObject();
        object.put("help_score_avg.cbm", 123);

        assertEquals(123,
                JSONPath.extract(
                        object.toJSONString(), "['help_score_avg.cbm']"));
    }
}
