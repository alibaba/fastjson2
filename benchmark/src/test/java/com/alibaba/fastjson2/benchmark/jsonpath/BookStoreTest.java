package com.alibaba.fastjson2.benchmark.jsonpath;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BookStoreTest {
    static final BookStore benchmark = new BookStore();
    static final int LOOP = 1000_000;

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 994
            // zulu11.64.19 : 998
            // zulu17.40.19 : 980
        }
    }

    public static void jayway() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jayway(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("cainiao millis : " + millis);
            // zulu8.68.0.21 : 2975
            // zulu11.64.19 : 2417
            // zulu17.40.19 : 2117
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        fastjson2Compile();
//        jayway();
    }
}
