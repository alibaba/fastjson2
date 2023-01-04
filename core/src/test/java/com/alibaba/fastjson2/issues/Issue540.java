package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class Issue540 {
    @Test
    public void test() {
        String str = "{\"num\":\"2147483650\"}";
        B b = JSONObject.parseObject(str, B.class);
        System.out.println(b.num);
    }

    @Data
    class B {
        BigDecimal num;
    }
}
