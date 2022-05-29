package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue371 {
    @Test
    public void test() {
        String userInfoBodyString = "{\"errcode\":0,\"errmsg\":\"OK\",\"weixiao_stu_id\":\"mmmmm\",\"card_number\":\"1111111\",\"name\":\"\\u5f20\\u7a0b\\u6d69\",\"identity_type\":1,\"data_source\":0,\"ocode\":\"1111111\"}";
        WeixiaoUserInfoResp userInfoBody = JSON.parseObject(userInfoBodyString, WeixiaoUserInfoResp.class);
        assertEquals("1111111", userInfoBody.getCard_number());

        byte[] bytes = userInfoBodyString.getBytes(StandardCharsets.UTF_8);
        WeixiaoUserInfoResp userInfoBody1 = JSON.parseObject(bytes, WeixiaoUserInfoResp.class);
        assertEquals("1111111", userInfoBody1.getCard_number());

        JSONReader jsonReaderStr = TestUtils.createJSONReaderStr(userInfoBodyString);
        WeixiaoUserInfoResp userInfoBody2 = jsonReaderStr.read(WeixiaoUserInfoResp.class);
        assertEquals("1111111", userInfoBody2.getCard_number());
    }

    public static class WeixiaoUserInfoResp {
        @Override
        public String toString() {
            return "WeixiaoUserInfoResp{" +
                    "card_number='" + card_number + '\'' +
                    '}';
        }

        String card_number;

        public String getCard_number() {
            return card_number;
        }

        public void setCard_number(String card_number) {
            this.card_number = card_number;
        }
    }
}
