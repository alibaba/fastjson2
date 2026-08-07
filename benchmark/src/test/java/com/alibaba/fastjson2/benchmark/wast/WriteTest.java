package com.alibaba.fastjson2.benchmark.wast;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class WriteTest {
    public static void fastjson2_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2();
        }
    }

    public static void fastjson2() {
        WriteCase benchmark = new WriteCase();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Write-fastjson2 : " + millis);

        // zulu8.62.0.19 : 2161
        // zulu11.52.13 : 2170 1973
        // zulu17.32.13 :
        // zulu18.28.13 : 1977
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void wastjson_test() {
        for (int i = 0; i < 10; i++) {
            wastjson();
        }
    }

    public static void wastjson() {
        WriteCase benchmark = new WriteCase();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; ++i) {
            benchmark.wastjson(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Write-wast : " + millis);

        // zulu8.62.0.19 : 1621
        // zulu11.52.13 : 1511
        // zulu17.32.13 :
        // zulu18.28.13 : 1558
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_test();
        wastjson_test();
    }
}
