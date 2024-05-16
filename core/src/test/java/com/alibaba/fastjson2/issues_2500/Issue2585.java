package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2585 {
    @Test
    public void testMutant1() {
        String str1 = "{\"data\": [1]}";
        String str2 = "{\n" +
                "  \"code\": \"1003\", \n" +
                "  \"data\": [1], \n" +
                "  \"message\": \"code: 1003,以【你好】开头的句子的长度不符合要求, 长度限制：3~500，实际长度：2\"\n" +
                "}\n";
        Object obj1 = JSONPath.eval(
                str1, "$.data[0][0]");
        Object obj2 = JSONPath.eval(
                str2, "$.data[0][0]");
        assertEquals(obj1, obj2);
    }

    @Test
    public void testMutant2() {
        String str1 = "{\"data\": [1,2]}";
        String str2 = "{\n" +
                "  \"code\": \"1003\", \n" +
                "  \"data\": [1,2], \n" +
                "  \"message\": \"code: 1003,以【你好】开头的句子的长度不符合要求, 长度限制：3~500，实际长度：2\"\n" +
                "}\n";
        System.out.println(str1);
        System.out.println(str2);
        assertEquals(
                JSONPath.eval(str1, "$.data[0][0]"),
                JSONPath.eval(str2, "$.data[0][0]"));
    }

    @Test
    public void testMutant3() {
        String str1 = "{\"data\": [{\"id\":\"1\"}]}";
        String str2 = "{\n" +
                "  \"code\": \"1003\", \n" +
                "  \"data\": [{\"id\":\"1\"}], \n" +
                "  \"message\": \"code: 1003,以【你好】开头的句子的长度不符合要求, 长度限制：3~500，实际长度：2\"\n" +
                "}\n";
        System.out.println(str1);
        System.out.println(str2);
        assertEquals(
                JSONPath.eval(str1, "$.data[0][0]"),
                JSONPath.eval(str2, "$.data[0][0]"));
    }
}
