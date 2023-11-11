package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class Issue2004 {
    @Test
    public void test() {
        String str = "{\"val\":12.34F}";
        JSON.parseObject(str, Bean.class);
        JSON.parseObject(str.toCharArray(), Bean.class);
        JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
    }

    public static class Bean {
    }
}
