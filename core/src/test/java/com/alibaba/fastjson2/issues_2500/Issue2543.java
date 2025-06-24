package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2543 {
    @Test
    public void test() {
        String str = "{\"amt\":22.12}";
        assertEquals(
                JSON.toJSONString(JSON.parseObject(str)),
                JSON.toJSONString(JSON.parseObject(str.getBytes(StandardCharsets.UTF_8)))
        );
    }
}
