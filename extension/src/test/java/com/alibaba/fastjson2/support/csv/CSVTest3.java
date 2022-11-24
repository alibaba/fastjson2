package com.alibaba.fastjson2.support.csv;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class CSVTest3 {
    @Test
    public void test() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2017, 5, 4, 12, 13, 14);
        ZonedDateTime zdt = ldt.atZone(IOUtils.DEFAULT_ZONE_ID);
        Instant instant = zdt.toInstant();
        Date date = new Date(instant.toEpochMilli());

        Object[] row = new Object[]{
                Byte.MIN_VALUE, Byte.MAX_VALUE,
                Short.MIN_VALUE, Short.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE,
                Float.MIN_VALUE, Float.MAX_VALUE,
                Double.MIN_VALUE, Double.MAX_VALUE,
                BigDecimal.valueOf(123), BigInteger.valueOf(123),
                "abc", "中国",
                LocalDate.of(2099, 1, 21), LocalDate.of(1970, 1, 25),
                ldt, zdt, instant, date
        };
        Type[] types = new Type[row.length];
        for (int i = 0; i < row.length; i++) {
            types[i] = row[i].getClass();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVWriter writer = CSVWriter.of(out);
        writer.writeRow(row);
        writer.close();

        byte[] bytes = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        CSVParser parser = CSVParser.of(in, types);
        Object[] row1 = parser.readLineValues();
        assertArrayEquals(row, row1);
    }
}
