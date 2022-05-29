package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 18/06/2017.
 */
public class Issue1272_IgnoreError {
    @Test
    public void test_for_issue() throws Exception {
        String text = JSON.toJSONString(new Point(), JSONWriter.Feature.IgnoreErrorGetter);
        assertEquals("{}", text);
    }

    public static class Point {
        private Long userId;

        public long getUserId() {
            return userId;
        }
    }
}
