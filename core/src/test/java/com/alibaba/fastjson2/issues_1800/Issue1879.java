package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1879 {
    @Test
    public void test() {
        TestResponse payload = new TestResponse();
        TestResponse result = JSON.parseObject(JSON.toJSONBytes(payload), TestResponse.class);
        assertNotNull(result);
    }

    static class TestResponse {
        private TestContext testContext;

        public TestContext getTestContext() {
            return testContext;
        }

        public void setTestContext(TestContext testContext) {
            this.testContext = testContext;
        }

        public static class TestContext {
            public TestContext() {
            }

            private String userInfo = "";

            public String getUserInfo() {
                return userInfo;
            }

            public void setUserInfo(String userInfo) {
                this.userInfo = userInfo;
            }
        }
    }
}
