package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class Issue2955 {
    @Test
    public void test() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        long STRING_LENGTH = 63L * 1024L * 1024L;

        JSONObject jsonObject = new JSONObject();
        System.out.println(STRING_LENGTH);
        // 生成64 * 1024 * 1024长度的字符串
        Random random = new Random();
        StringBuilder largeStringBuilder = new StringBuilder();
        for (long i = 0; i < STRING_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            largeStringBuilder.append(CHARACTERS.charAt(index));
        }
        String largeString = largeStringBuilder.toString();
        jsonObject.put("largeString", largeString);
        String jsonString = jsonObject.toJSONString();
    }
}
