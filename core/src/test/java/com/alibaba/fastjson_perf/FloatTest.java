package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.Float10;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class FloatTest {
    Float10 date1 = new Float10();

    public FloatTest() {
        Random random = new Random();

        date1.setV0000(random.nextFloat());
        date1.setV0001(random.nextFloat());
        date1.setV0002(random.nextFloat());
        date1.setV0003(random.nextFloat());
        date1.setV0004(random.nextFloat());
        date1.setV0005(random.nextFloat());
        date1.setV0006(random.nextFloat());
        date1.setV0007(random.nextFloat());
        date1.setV0008(random.nextFloat());
        date1.setV0009(random.nextFloat());
    }

    @Test
    public void test_date_0() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSON.toJSONString(date1);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-asm millis : " + millis);
            // JDK 1.8_311 : 294
        }
    }

    @Test
    public void test_date_0_utf8() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSONWriter writer = JSONWriter.ofUTF8();
                writer.writeAny(date1);
                writer.getBytes();
                writer.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-asm-utf8 millis : " + millis);
            // JDK 1.8_311 : 552 545
        }
    }

    @Test
    public void test_write_0_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                mapper.writeValueAsString(date1);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-jackson millis : " + millis);
            // JDK 1.8_311 : 428
        }
        System.out.println();
    }

    @Test
    public void test_write_0_utf8_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                mapper.writeValueAsBytes(date1);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-jackson-utf8 millis : " + millis);
            // JDK 1.8_311 : 918
        }
        System.out.println();
    }
}
