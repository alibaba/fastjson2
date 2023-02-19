package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class LongTextParse2Test {
    static final LongTextParse2 benchmark = new LongTextParse2();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LongTextParseCase-fastjson2 : " + millis);

            // zulu8.68.0.21 : 801
            // zulu11.52.13 : 738
            // zulu17.32.13 :
        }
    }

    public static void wastjson() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LongTextParseCase-wastjson : " + millis);

            // zulu8.68.0.21 : 797
            // zulu11.52.13 : 814
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        wastjson();
    }
}
