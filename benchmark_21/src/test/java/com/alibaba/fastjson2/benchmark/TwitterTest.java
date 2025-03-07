package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.benchmark.simdjson.Twitter;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class TwitterTest {
    private static final Twitter benchmark = new Twitter();

    public static void fastjson2_parse() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.fastjson2_parse(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Twitter-fastjson2_parse : " + millis);
            // zulu21.32.17 520
        }
    }

    public static void wast_parse() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                benchmark.wast_parse(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("Twitter-wast_parse : " + millis);
            // zulu21.32.17 463
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_parse();
        wast_parse();
    }
}
