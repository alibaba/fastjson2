package com.alibaba.fastjson2.internal.processor.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3836 {
    @Test
    public void test() {
        String json = JSON.toJSONString(new MyDto());
        assertEquals("{}", json);
    }

    @JSONCompiled
    @JSONType
    public static class MyDto {
        public String abc;
    }
}
