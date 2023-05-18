package com.alibaba.fastjson2.benchmark.jjb;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ClientsWriteUTF8BytesTest {
    static final ClientsWriteUTF8Bytes benchmark = new ClientsWriteUTF8Bytes();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ClientsWriteUTF8Bytes-fastjson2 millis : " + millis);
            // zulu17.40.19 : 1925 1852 1793 1774 1765 1777 1708 1699 1520 1506 1498 1485 1474 1450 1405
            // oracle-jdk-17.0.6 :
            // oracle-jdk-17.0.6_vec :
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
//        dsljson();
//        jackson();
    }
}
