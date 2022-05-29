package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1423 {
    @Test
    public void test_for_issue() {
        Exception error = null;
        try {
            JSON.parseObject("{\"v\":9223372036854775808}", LongVal.class);
        } catch (JSONException e) {
            e.printStackTrace();
            error = e;
        }
        assertNotNull(error);
        error.printStackTrace();
    }

    @Test
    public void test_for_issue_arrayMapping() {
        Exception error = null;
        try {
            JSON.parseObject("[9223372036854775808]", LongVal.class, com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean);
        } catch (JSONException e) {
            error = e;
        }
        assertNotNull(error);
    }

    public static class LongVal {
        private long v;

        public void setV(long v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return String.valueOf(v);
        }
    }
}
