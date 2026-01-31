package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Issue3283 {
    static class Bean {
        private boolean test;

        public boolean isTest() {
            return test;
        }

        public void setTest(boolean test) {
            this.test = test;
        }
    }

    @Test
    public void singleQuote() {
        String text = "{'test': 'true'}";
        Bean bean = JSONObject.parseObject(text, Bean.class);
        Assertions.assertTrue(bean.isTest());

        text = "{'test': ''}";
        bean = JSONObject.parseObject(text, Bean.class);
        Assertions.assertFalse(bean.isTest());

        text = "{'test': 'Y'}";
        bean = JSONObject.parseObject(text, Bean.class);
        Assertions.assertTrue(bean.isTest());

        text = "{'test': '1'}";
        bean = JSONObject.parseObject(text, Bean.class);
        Assertions.assertTrue(bean.isTest());
    }
}
