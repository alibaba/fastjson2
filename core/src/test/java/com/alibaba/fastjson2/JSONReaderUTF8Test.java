package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderUTF8Test {
    @Test
    public void notSupported() {
        String str = "'2018-07-14Z'";
        LocalDate localDate = LocalDate.of(2018, 7, 14);
        {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            assertEquals(localDate, jsonReader.readLocalDate11());
        }
        {
            char[] chars = str.toCharArray();
            JSONReader jsonReader = JSONReader.of(chars, 0, chars.length);
            assertEquals(localDate, jsonReader.readLocalDate11());
        }
        {
            byte[] bytes = JSONB.toBytes((String) JSON.parse(str));
            assertEquals(localDate, JSONB.parseObject(bytes, LocalDate.class));
        }
        {
            byte[] bytes = JSONB.toBytes((String) JSON.parse(str), StandardCharsets.UTF_8);
            assertEquals(localDate, JSONB.parseObject(bytes, LocalDate.class));
        }
    }
}
