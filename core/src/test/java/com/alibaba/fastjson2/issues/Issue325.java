package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue325 {
    @Test
    public void parse() {
        String text = "{\"IsSuccess\":true,\"Data\":\"1fd1717837744bd2ba967167a6f6f417\",\"Message\":\"获取身份令牌成功\"}";
        JSONObject obj = JSON.parseObject(text);
        assertEquals(true, obj.getBoolean("IsSuccess"));
        assertEquals("1fd1717837744bd2ba967167a6f6f417", obj.getString("Data"));
        assertEquals("获取身份令牌成功", obj.getString("Message"));

        //转换为 POJO
        TokenResult result = JSON.parseObject(text, TokenResult.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(true, result.isIsSuccess());
        assertEquals("1fd1717837744bd2ba967167a6f6f417", result.getData());
        assertEquals("获取身份令牌成功", result.getMessage());
    }

    public static class TokenResult {
        String Data;
        String Message;
        boolean IsSuccess;

        public String getData() {
            return Data;
        }

        public TokenResult setData(String data) {
            Data = data;
            return this;
        }

        public String getMessage() {
            return Message;
        }

        public TokenResult setMessage(String message) {
            Message = message;
            return this;
        }

        public boolean isIsSuccess() {
            return IsSuccess;
        }

        public TokenResult setIsSuccess(boolean success) {
            IsSuccess = success;
            return this;
        }
    }
}
