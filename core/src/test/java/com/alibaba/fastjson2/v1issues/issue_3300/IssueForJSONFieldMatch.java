package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IssueForJSONFieldMatch {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals(123,
                JSON.parseObject("{\"user_id\":123}", VO.class)
                        .userId);
        assertEquals(123,
                JSON.parseObject("{\"userId\":123}", VO.class)
                        .userId);
        assertEquals(123,
                JSON.parseObject("{\"user-id\":123}", VO.class)
                        .userId);
    }

    public static class VO {
        @JSONField(name = "user_id", alternateNames = {"userId", "user-id"})
        public int userId;
    }
}
