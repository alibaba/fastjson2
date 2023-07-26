package com.alibaba.fastjson2.benchmark.jjb;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ClientsWriteUTF8BytesTest {
    static final ClientsWriteUTF8Bytes benchmark = new ClientsWriteUTF8Bytes();

    public static void jsonb() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-jsonb millis : " + millis);
            // zulu17.40.19 : 609
            // zulu17.40.19_vec :
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
            // zulu17.40.19 : 311 303
            // zulu17.40.19_vec :
        }
    }

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-fastjson2 millis : " + millis);
            // zulu8.70.0.23 : 1533 1493 1374
            // zulu17.40.19 : 1419 1361 1356 1356 1317 1224 1212 1202
            // zulu17.40.19_vec : 1116
            // zulu17.40.19_reflect : 1427
        }
    }

    public static void fastjson2_str() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2_str(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-fastjson2 millis : " + millis);
            // zulu17.40.19 :
            // zulu17.40.19_vec : 1139
        }
    }

    public static void dsljson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.dsljson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-dsljson millis : " + millis);
            // zulu17.40.19 : 2169 1487
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
//        jsonb();
//        jsonb_beanToArray();
//        fastjson2_str();
//        dsljson();
//        jackson();
    }
}
