package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class Issue474 {
    @Test
    public void test() {
        Item item1 = new Item("item1");
        Item item2 = new Item("item2");
        Result result = new Result(Arrays.asList(item1, item2));
        Response<Result> response = new Response<>(result);
        String json = JSON.toJSONString(response);

        System.out.println(fastJsonParse(json, Result.class));
        System.out.println(fastJson2Parse(json, Result.class));
    }

    static <T extends AbstractResult> T fastJson2Parse(String json, Class<?> clazz) {
        TypeReference<Response<T>> typeReference = new TypeReference<Response<T>>(clazz) {
        };
        Response<T> result = JSON.parseObject(json, typeReference);
        return result.getData();
    }

    static <T extends AbstractResult> T fastJsonParse(String json, Class<?> clazz) {
        com.alibaba.fastjson.TypeReference<Response<T>> typeReference = new com.alibaba.fastjson.TypeReference<Response<T>>(clazz) {
        };
        Response<T> result = com.alibaba.fastjson.JSON.parseObject(json, typeReference);
        return result.getData();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response<T extends AbstractResult> {
        private T data;
    }

    public abstract static class AbstractResult {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result
            extends AbstractResult {
        List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String name;
    }
}
