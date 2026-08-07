package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Collections;

public class Issue661 {
    @Data
    static class Response<T> {
        T data;

        Response(T data) {
            this.data = data;
        }
    }

    @Data
    static class A {
        // 没有属性不会异常
        int a;
    }

    @RepeatedTest(100)
    void testB() {
        for (int i = 0; i < 1000; i++) {
            JSON.toJSONString(new Response<>(new A()));
        }
    }

    @RepeatedTest(100)
    void testList() {
        for (int i = 0; i < 1000; i++) {
            // Collections.singletonList(0)改成0也会异常，异常概率小一些
            JSON.toJSONString(new Response<>(Collections.singletonList(0)));
        }
    }
}
