package com.alibaba.fastjson2.benchmark.primitves;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BigInteger20Test {
    static final BigInteger20 benchmark = new BigInteger20();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("BigDecimal20-fastjson2 : " + millis);

            // zulu8.68.0.21 : 367
            // zulu11.52.13 :
            // zulu17.32.13 :
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

            // zulu8.62.0.19 :
            // zulu11.52.13 :
            // zulu17.40.19 :
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
            // zulu8.62.0.19 :
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
            // zulu8.62.0.19 :
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
