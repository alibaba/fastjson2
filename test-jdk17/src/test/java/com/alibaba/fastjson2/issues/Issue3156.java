package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3156 {
    @Test
    public void test() {
        String json = "{\"code\":200,\"data\":{\"userId\":1234556,\"name\":\"张三\"}}";

        String json2 = "{\"code\":200,\"data\":{\"userId\":1234556,\"name\":\"张三\"},\"data2\":{\"userId\":1524231,\"name\":\"李四\"}}";

        String json3 = "{\"code\":200,\"datas\":[{\"userId\":1,\"name\":\"a\"},{\"userId\":2,\"name\":\"b\"}]}";

        String json4 = "{\"code\":200,\"data\":{\"info\":[{\"userId\":7,\"name\":\"x\"}]}}";

        AjaxResult<LoginResult> r = JSON.parseObject(json, new TypeReference<AjaxResult<LoginResult>>() {
        });
        assertEquals(200, r.code());
        assertEquals("LoginResult", r.data().getClass().getSimpleName());
        assertEquals("张三", r.data().name);

        AjaxResult2<LoginResult, LogoutResult> r2 = JSON.parseObject(json2, new TypeReference<AjaxResult2<LoginResult, LogoutResult>>() {
        });
        assertEquals("LoginResult", r2.data().getClass().getSimpleName());
        assertEquals("LogoutResult", r2.data2().getClass().getSimpleName());

        ListResult<LoginResult> r3 = JSON.parseObject(json3, new TypeReference<ListResult<LoginResult>>() {
        });
        assertEquals(2, r3.datas().size());
        assertEquals("b", r3.datas().get(1).name);

        MapListResult<String, LoginResult> r4 = JSON.parseObject(json4, new TypeReference<MapListResult<String, LoginResult>>() {
        });
        assertEquals("LoginResult", r4.data().get("info").get(0).getClass().getSimpleName());
    }

    @Data
    public static class LoginResult {
        private Long userId;
        private String name;
    }

    @Data
    public static class LogoutResult {
        private Long userId;
        private String name;
    }

    public record AjaxResult<T>(int code, String msg, T data) {
    }

    public record AjaxResult2<A, B>(int code, String msg, A data, B data2) {
    }

    public record ListResult<T>(int code, String msg, List<T> datas) {
    }

    public record MapListResult<K, V>(int code, String msg, Map<String, List<V>> data) {
    }

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
