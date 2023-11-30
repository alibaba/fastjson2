package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2059 {
    @Test
    public void test() {
        String str1 = "{'queryParam':{ 'STATUS': '1','MENU_TYPE': '11'}}";
        assertTrue(
                JSON.isValid(str1));
        assertTrue(
                JSON.isValid(str1.toCharArray()));
        assertTrue(
                JSON.isValid(
                        str1.getBytes(StandardCharsets.UTF_8)));
        assertTrue(
                JSON.isValid(
                        str1.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.ISO_8859_1));
        assertTrue(
                JSON.isValid(
                        str1.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.US_ASCII));
    }
}
