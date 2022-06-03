package com.alibaba.fastjson_perf.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

public class JSONCompilePerf {
    Bean bean = new Bean();
    JSONPath path = JSONPath.compile("$.id");
    JSONPath path1 = JSONPath.compile("$.id", Bean.class);

    @Test
    public void test_perf() {
        for (int i = 0; i < 10; ++i) {
            perf();
            // JDK8 94
        }
    }

    @Test
    public void test_perf1() {
        for (int i = 0; i < 10; ++i) {
            perf1();
            // JDK8 28
        }
    }

    public void perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 10; ++i) {
            path.setInt(bean, i);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println(millis); // 447 438 361
    }

    public void perf1() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000 * 10; ++i) {
            path1.setInt(bean, i);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println(millis); // 447 438 361
    }

    public static class Bean {
        private int id;
        public Item item;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class Item {
        public int id;
    }
}
