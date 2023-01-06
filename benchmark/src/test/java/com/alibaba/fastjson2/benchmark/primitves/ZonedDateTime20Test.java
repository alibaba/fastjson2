package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ZonedDateTime20Test {
    static final ZonedDateTime20 benchmark = new ZonedDateTime20();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ZonedDateTime20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 1518 1107 1089
            // zulu11.52.13 : 1042
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void fastjson2_jsonb() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2_jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ZonedDateTime20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 682
            // zulu11.52.13 : 662
            // zulu17.32.13 : 630
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void fastjson1() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ZonedDateTime20-fastjson1 : " + millis);
            // zulu8.62.0.19 : 7112
            // zulu11.52.13 :
            // zulu17.32.13 :
            // zulu18.28.13 :
            // zulu19.0.47 :
            // corretto-8 :
            // corretto-11 :
            // corretto-17 :
            // corretto-18 :
            // oracle-jdk-17.0.4 :
            // oracle-jdk-18.0.2 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        fastjson2_jsonb();
//        fastjson1();
    }
}
