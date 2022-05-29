package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1227 {
    @Test
    public void test_for_issue() throws Exception {
        String t2 = "{\"state\":2,\"msg\":\"\ufeffmsg2222\",\"data\":[]}";

        try {
            Bean model = JSON.parseObject(t2, Bean.class);
            assertEquals("\uFEFFmsg2222", model.msg);

            model.msg = "\uFEFFss";
            String t3 = JSON.toJSONString(model);
            assertTrue(t3.contains(model.msg));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static class Bean {
        public int state;
        public String msg;
    }
}
