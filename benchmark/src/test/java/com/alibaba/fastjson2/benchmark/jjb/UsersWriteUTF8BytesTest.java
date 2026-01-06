package com.alibaba.fastjson2.benchmark.jjb;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class UsersWriteUTF8BytesTest {
    static final UsersWriteUTF8Bytes benchmark = new UsersWriteUTF8Bytes();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("UsersWriteUTF8BytesTest-fastjson2 millis : " + millis);
            // zulu17.40.19 : 1664 1657 1650 1591 1564 1445 1377 1085
            // oracle-jdk-17.0.6 :
            // oracle-jdk-17.0.6_vec : 1230
            // zulu17-jdk-17.40.19_vec : 1055
        }
    }

    public static void wast() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.wast(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("wast millis : " + millis);
            // zulu17.40.19 : 1186
        }
    }

    public static void fastjson2_str() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2_str(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu17.40.19 : 1262
            // oracle-jdk-17.0.6 :
            // oracle-jdk-17.0.6_vec :
            // zulu17-jdk-17.40.19_vec : 1087
        }
    }

    public static void jsonb_beanToArray() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb_beanToArray(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-jsonb_beanToArray millis : " + millis);
            // zulu17.40.19 : 371
            // zulu17.40.19_vec :
        }
    }

    public static void dsljson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.dsljson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("dsljson millis : " + millis);
            // zulu17.40.19 : 2251 1546
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson millis : " + millis);
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        wast();
//        fastjson2_str();
//        jsonb_beanToArray();
//        dsljson();
//        jackson();
    }
}
