package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamingTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.oAuth = "abc";

        assertEquals("{\"oAuth\":\"abc\"}", JSON.toJSONString(bean));
    }

    public static class Bean {
        private String oAuth;

        public String getOAuth() {
            return oAuth;
        }

        public void setOAuth(String oAuth) {
            this.oAuth = oAuth;
        }
    }
}
