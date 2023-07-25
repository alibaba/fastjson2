package com.alibaba.fastjson2.benchmark.twitter;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class TwitterParseStringTest {
    static final int LOOP = 1_000_000;
    static final TwitterParseString benchmark = new TwitterParseString();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.70.0.23 : 4128
            // zulu11.64.19 :
            // zulu17.42.19 :
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
            // zulu8.70.0.23 : 7605
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        jackson();
//        fastjson1();
//        gson();
//        wastjson();
    }
}
