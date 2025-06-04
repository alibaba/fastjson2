package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.NameFilter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3141 {
    @Test
    public void test() {
        NameFilter nf = (object, name, value) -> {
            if ("seq".equals(name)) {
                return "id";
            }
            return name;
        };
        assertEquals(2, JSON.parseObject("{\"id\": 2}", Ikun.class, nf).seq);
    }

    @Data
    public static class Ikun {
        private Long seq;
    }
}
