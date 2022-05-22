package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IssueForJSONFieldMatch {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals(123,
                JSON.parseObject("{\"user_Id\":123}", VO.class)
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
