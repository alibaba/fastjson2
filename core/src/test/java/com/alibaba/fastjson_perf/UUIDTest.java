package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.UUID1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

public class UUIDTest {
    UUID1 date1 = new UUID1();

    public UUIDTest() {
        date1.setId(UUID.randomUUID());
    }

    @Test
    public void test_uuid_0() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONWriter writer = JSONWriter.of();
                writer.writeAny(date1);
                writer.toString();
                writer.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("UUID1-asm millis : " + millis);
            // JDK 1.8_311 : 1848 1470 1070 947 760 717 700 620
        }
    }

    @Test
    public void test_uuid_0_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                mapper.writeValueAsString(date1);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("UUID1-jackson millis : " + millis);
            // JDK 1.8_311 : 1282
        }
        System.out.println();
    }



}
