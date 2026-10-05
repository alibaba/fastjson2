package com.alibaba.fastjson2.benchmark;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartItemPublicationRegressionTest {
    @Test
    public void concurrentInitialization() throws Exception {
        Field cache = CartItemDO2Benchmark.class.getDeclaredField("list");
        cache.setAccessible(true);
        Method factory = CartItemDO2Benchmark.class.getDeclaredMethod("newCartsItem");
        factory.setAccessible(true);
        ExecutorService executor = Executors.newFixedThreadPool(16);
        try {
            for (int round = 0; round < 20; round++) {
                cache.set(null, null);
                CountDownLatch start = new CountDownLatch(1);
                List<Future<Integer>> sizes = new ArrayList<>();
                for (int thread = 0; thread < 16; thread++) {
                    sizes.add(executor.submit(() -> {
                        start.await();
                        return ((List<?>) factory.invoke(null)).size();
                    }));
                }
                start.countDown();
                for (Future<Integer> size : sizes) {
                    assertEquals(1000, size.get().intValue());
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
