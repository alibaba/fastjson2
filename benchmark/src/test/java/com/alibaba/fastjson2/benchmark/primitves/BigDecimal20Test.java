package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BigDecimal20Test {
    static final BigDecimal20 benchmark = new BigDecimal20();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.62.0.19 : 1245 742
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

    public static void jsonb() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-jsonb : " + millis);

            // zulu8.62.0.19 : 276
            // zulu11.52.13 : 236
            // zulu17.40.19 : 234
        }
    }

    public static void kryo() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.kryo(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-kryo : " + millis);

            // zulu8.62.0.19 : 513
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void hessian() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.hessian(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-hessian : " + millis);

            // zulu8.62.0.19 : 2953
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void fury() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fury(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fury : " + millis);

            // zulu8.62.0.19 : 480
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-jackson : " + millis);
            // zulu8.62.0.19 : 1280
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void wastjson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.wastjson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-wastjson : " + millis);
            // zulu8.62.0.19 : 675
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        jsonb();
//        hessian();
//        fury();
//        jackson();
//        wastjson();
//        kryo();
    }
}
