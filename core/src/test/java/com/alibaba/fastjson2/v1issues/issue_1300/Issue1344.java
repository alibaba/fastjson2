package com.alibaba.fastjson2.v1issues.issue_1300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.junit.jupiter.api.Test;

/**
 * Created by wenshao on 26/07/2017.
 */
public class Issue1344 {
    @Test
    public void test_for_issue() throws Exception {
        TestException testException = new TestException("aaa");
        System.out.println("before：" + testException.getMessage());
        String json = JSON.toJSONString(testException);
        System.out.println(json);
        TestException o = JSON.parseObject(json, TestException.class);
        System.out.println("after：" + o.getMessage());
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
