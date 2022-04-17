package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.Date1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateTest {
    Date1 date1 = new Date1();

    public DateTest() {
        date1.setDate(new Date());
    }

    @Test
    public void test_date_0() {
        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                JSONWriter writer = JSONWriter.of();
                writer.writeAny(date1);
                writer.toString();
                writer.close();
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-asm millis : " + millis);
            // JDK 1.8_311 : 1848 1470 1070 947 760 717 700
        }
    }

    @Test
    public void test_write_0_jackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();

            for (int j = 0; j < 1000 * 1000 * 10; ++j) {
                mapper.writeValueAsString(date1);
            }

            long millis = System.currentTimeMillis() - start;
            System.out.println("Date1-jackson millis : " + millis);
            // JDK 1.8_311 : 1282
        }
        System.out.println();
    }
}
