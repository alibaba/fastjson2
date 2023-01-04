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
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_8);
        assertEquals(localDate, jsonReader.readLocalDate11());
    }
}
