package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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

    @Test
    public void test_2() { // 泛型 无默认构造器
        String json = "{\"temperature\":{\"enabled\":true,\"value\":0.1},\"presencePenalty\":{\"enabled\":false,\"value\":0},\"frequencyPenalty\":{\"enabled\":false,\"value\":0}}";
        Options options = JSONObject.parseObject(json, new TypeReference<Options>() {});
        Object val = options.getTemperature().value();
        Object val2 = options.getPresencePenalty().value();
        assertInstanceOf(Double.class, val);
        assertInstanceOf(Double.class, val2);
    }

    @Data
    public class Options {
        private Parameter<Double> temperature;
        private Parameter<Double> presencePenalty;
        private Parameter<Double> frequencyPenalty;
        public record Parameter<T extends Number>(Boolean enabled, T value) {}
    }

    @Test
    public void test_3() { // 泛型 有默认构造器
        String json = "{\"temperature\":{\"enabled\":true,\"value\":0.1},\"presencePenalty\":{\"enabled\":false,\"value\":0},\"frequencyPenalty\":{\"enabled\":false,\"value\":0}}";
        Options2 options = JSONObject.parseObject(json, new TypeReference<Options2>() {});
        Object val = options.getTemperature().getValue();
        Object val2 = options.getPresencePenalty().getValue();
        assertInstanceOf(Double.class, val);
        assertInstanceOf(Double.class, val2);
    }

    @Data
    public class Options2 {
        private Parameter<Double> temperature;
        private Parameter<Double> presencePenalty;
        private Parameter<Double> frequencyPenalty;

        @NoArgsConstructor
        @Data
        public class Parameter<T extends Number>{
            private Boolean enabled;
            private T value;
        }
    }
}
