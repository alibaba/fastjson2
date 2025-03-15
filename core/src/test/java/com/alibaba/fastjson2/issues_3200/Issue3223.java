package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3223 {
    @Test
    public void test() {
        String res = "{\n" +
                "    \"res\": \"Ñ\\n\"\n" +
                "}";
        ResBean resBean = JSON.parseObject(res, ResBean.class);
        assertEquals("Ñ\n", resBean.res);
    }

    public static class ResBean {
        private String res;

        public String getRes() {
            return res;
        }

        public void setRes(String res) {
            this.res = res;
        }
    }
}
