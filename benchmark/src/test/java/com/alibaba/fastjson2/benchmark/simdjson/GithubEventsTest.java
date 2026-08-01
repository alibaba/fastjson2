package com.alibaba.fastjson2.benchmark.simdjson;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class GithubEventsTest {
    private static final GithubEvents benchmark = new GithubEvents();

    public static void fastjson2_parse() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 10; ++i) {
                benchmark.fastjson2_parse(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("GithubEvents-fastjson2_parse : " + millis);
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2_parse();
    }
}
