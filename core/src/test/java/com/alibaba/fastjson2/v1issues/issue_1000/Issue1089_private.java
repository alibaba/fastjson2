package com.alibaba.fastjson2.v1issues.issue_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 20/03/2017.
 */
public class Issue1089_private {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"ab\":123,\"a_b\":456}";
        TestBean tb = JSON.parseObject(json, TestBean.class);
        assertEquals(123, tb.getAb());
    }

    private static class TestBean {
        private int ab;

        public int getAb() {
            return ab;
        }

        public void setAb(int ab) {
            this.ab = ab;
        }
    }
}
