package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue984 {
    @Test
    public void test() {
        List<String> list = JSON.parseArray("[\"abc\"]", String.class);
        assertEquals(1, list.size());
    }
}
