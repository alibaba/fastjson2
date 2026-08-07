package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

    @Test
    public void testParallel() {
        final Consumer consumer = e -> {};
        // 在ForkJoinPool中异常
        Stream.of(
                () -> {
                    for (int i = 0; i < 100000; i++) {
                        consumer.accept(
                                JSON.toJSONString(
                                        new Response<>(Collections.singletonList(0))
                                )
                        );
                    }
                },
                (Runnable) () -> {
                    for (int i = 0; i < 100000; i++) {
                        consumer.accept(
                                JSON.toJSONString(
                                        new Response<>(new A())
                                )
                        );
                    }
                })
                .parallel()
                .forEach(Runnable::run);
    }
}
