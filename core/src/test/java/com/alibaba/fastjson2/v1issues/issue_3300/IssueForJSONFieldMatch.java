package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import junit.framework.TestCase;

public class IssueForJSONFieldMatch extends TestCase {
    public void test_for_issue() throws Exception {
        assertEquals(123
                , JSON.parseObject("{\"user_id\":123}", VO.class)
                        .userId);
        assertEquals(123
                , JSON.parseObject("{\"userId\":123}", VO.class)
                        .userId);
        assertEquals(123
                , JSON.parseObject("{\"user-id\":123}", VO.class)
                        .userId);
    }

    public static class VO {
        @JSONField(name = "user_id", alternateNames = {"userId", "user-id"})
        public int userId;
    }
}
