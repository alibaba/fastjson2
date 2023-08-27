package com.alibaba.fastjson2.benchmark.geoip;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class GeoIPParseStringTest {
    static final int LOOP = 10_000_000;
    static final GeoIPParseString benchmark = new GeoIPParseString();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.70.0.23 :
            // zulu11.64.19 : 3675
            // zulu17.42.19 :
        }
    }

    public static void fastjson1() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.fastjson1(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson1 millis : " + millis);
            // zulu8.70.0.23 :
            // zulu11.52.13 :
            // zulu17.32.13 :
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
            // zulu8.70.0.23 :
            // zulu11.52.13 : 10050
            // zulu17.32.13 :
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseString-wastjson millis : " + millis);
            // zulu8.70.0.23 :
            // zulu11.64.19 :
            // zulu17.42.19 :
        }
    }

    public static void gson() throws Exception {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.gson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseString-gson millis : " + millis);
            // zulu8.70.0.23 :
            // zulu11.64.19 : 10417
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
//        jackson();
//        fastjson1();
        gson();
//        wastjson();
    }
}
