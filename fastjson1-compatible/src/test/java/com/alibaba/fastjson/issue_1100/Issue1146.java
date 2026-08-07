package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 14/04/2017.
 */
public class Issue1146 {
    @org.junit.jupiter.api.Test
    public void test_for_issue() throws Exception {
        String json = JSON.toJSONString(new Test());
        assertEquals("{\"zzz\":true}", json);
    }

    @JSONType(ignores = {"xxx", "yyy"})
    public static class Test {
        public boolean isXxx() {
            return true;
        }

        public Boolean getYyy() {
            return true;
        }

        public Boolean getZzz() {
            return true;
        }
    }
}
