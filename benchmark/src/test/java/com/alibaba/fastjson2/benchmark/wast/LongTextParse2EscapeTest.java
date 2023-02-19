package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class LongTextParse2EscapeTest {
    static final LongTextParse2Escape benchmark = new LongTextParse2Escape();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LongTextParse2Escape-fastjson2 : " + millis);

            // zulu8.68.0.21 : 698 620
            // zulu11.52.13 : 555
            // zulu17.32.13 : 551
        }
    }

    public static void wastjson() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LongTextParse2Escape-wastjson : " + millis);

            // zulu8.68.0.21 : 595
            // zulu11.52.13 : 909
            // zulu17.32.13 : 1361
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        wastjson();
    }
}
