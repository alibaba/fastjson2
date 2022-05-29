package com.alibaba.fastjson2.v1issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by wenshao on 26/07/2017.
 */
public class Issue1344 {
    @Test
    public void test_for_issue() throws Exception {
        TestException testException = new TestException("aaa");
        String json = JSON.toJSONString(testException);
        TestException o = JSON.parseObject(json, TestException.class);
        assertNull(o.getMessage());
    }

    public static class TestException
            extends Exception {
        @JSONCreator
        public TestException() {
        }

        public TestException(String data) {
            super("Data : " + data);
        }
    }
}
