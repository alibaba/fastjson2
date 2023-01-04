package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class LongTextParseCaseTest {
    static final LongTextParseCase benchmark = new LongTextParseCase();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10_000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("LongTextParseCase-fastjson2 : " + millis);

            // zulu8.62.0.19 : 1583 897 821 910
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 : 1228 806
            // corretto-17 : 811
            // corretto-18 :
            // oracle-jdk-17.0.4 : 802
            // oracle-jdk-18.0.2 : 802
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

            // zulu8.62.0.19 : 789 1061 790
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 : 1070
            // corretto-17 : 1077
            // corretto-18 :
            // oracle-jdk-17.0.4 : 1068
            // oracle-jdk-18.0.2 : 1062
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        wastjson();
    }
}
