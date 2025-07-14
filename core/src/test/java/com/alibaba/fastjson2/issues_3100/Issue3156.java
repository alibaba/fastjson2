package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

public class Issue3156 {
    @Test
    public void test() {
        String json = "{\n" +
                "  \"code\": 200,\n" +
                "  \"data\": {\n" +
                "    \"userId\": 1524231,\n" +
                "    \"name\": \"张三\"\n" +
                "  }\n" +
                "}";

        AjaxResult<LoginResult> loginResultAjaxResult = JSON.parseObject(json, new TypeReference<AjaxResult<LoginResult>>(){});
        System.out.println("loginResultAjaxResult = " + loginResultAjaxResult.data().getClass().getName());
    }

    public static class LoginResult {
        private Long userId;
        private String name;
    }

//    public record AjaxResult<T>(int code, String msg, LoginResult data) {}
    public record AjaxResult<T>(int code, String msg, T data) {}

//    public static class AjaxResult<T> {
//        private int code;
//        private String msg;
//        private T data;
//
//        public AjaxResult() {
//        }
//
//        public int getCode() {
//            return code;
//        }
//
//        public AjaxResult<T> setCode(int code) {
//            this.code = code;
//            return this;
//        }
//
//        public String getMsg() {
//            return msg;
//        }
//
//        public AjaxResult<T> setMsg(String msg) {
//            this.msg = msg;
//            return this;
//        }
//
//        public T getData() {
//            return data;
//        }
//
//        public AjaxResult<T> setData(T data) {
//            this.data = data;
//            return this;
//        }
//    }
}
