package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.DoubleField10;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class DoubleTest {
    DoubleField10 bean = new DoubleField10();

    public DoubleTest() {
        Random random = new Random();

        bean.v000 = random.nextDouble();
        bean.v001 = random.nextDouble();
        bean.v002 = random.nextDouble();
        bean.v003 = random.nextDouble();
        bean.v004 = random.nextDouble();
        bean.v005 = random.nextDouble();
        bean.v006 = random.nextDouble();
        bean.v007 = random.nextDouble();
        bean.v008 = random.nextDouble();
        bean.v009 = random.nextDouble();
    }

    @Test
    public void test_writeString() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSON.toJSONString(bean);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2-writeString millis : " + millis);
            // JDK 1.8_311 : 427
        }
    }

    @Test
    public void test_writeUTF8() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                JSONWriter writer = JSONWriter.ofUTF8();
                writer.writeAny(bean);
                writer.getBytes();
                writer.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2-writeUTF8 millis : " + millis);
            // JDK 1.8_311 : 552 545
        }
    }

    @Test
    public void test_writeString_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                mapper.writeValueAsString(bean);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson-writeString millis : " + millis);
            // JDK 1.8_311 : 428
        }
        System.out.println();
    }

    @Test
    public void test_writeUTF8_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 1; ++j) {
                mapper.writeValueAsBytes(bean);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("jackson-writeUTF8 millis : " + millis);
            // JDK 1.8_311 : 918
        }
        System.out.println();
    }
}
