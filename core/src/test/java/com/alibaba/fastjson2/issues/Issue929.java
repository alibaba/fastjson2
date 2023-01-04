package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue929 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.isSuccess = true;
        String string = JSON.toJSONString(bean);
        assertEquals("{\"success\":true}", string);
        Bean bean1 = JSON.parseObject(string, Bean.class);
        assertEquals(bean.isSuccess, bean1.isSuccess);
    }

    public static class Bean {
        private Boolean isSuccess;

        public Boolean getSuccess() {
            return isSuccess;
        }

        public void setSuccess(Boolean success) {
            isSuccess = success;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.isSuccess = true;
        String string = JSON.toJSONString(bean);
        assertEquals("{\"success\":true}", string);
        Bean1 bean1 = JSON.parseObject(string, Bean1.class);
        assertEquals(bean.isSuccess, bean1.isSuccess);
    }

    @Test
    public void test1WithFastJson2() {
        Bean1 bean = new Bean1();
        bean.isSuccess = true;
        Bean1 bean1 = JSON.parseObject("{\"isSuccess\":true}", Bean1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(bean.isSuccess, bean1.isSuccess);
    }

    public static class Bean1 {
        private Boolean isSuccess;

        public Boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(Boolean success) {
            isSuccess = success;
        }
    }
}
