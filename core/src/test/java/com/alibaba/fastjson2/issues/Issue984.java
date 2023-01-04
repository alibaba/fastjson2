package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue984 {
    @Test
    public void test() {
        List<String> list = new ArrayList<>();
        for (String str : JSON.parseArray("[\"abc\"]", String.class)) {
            list.add(str);
        }
        assertEquals(1, list.size());
    }
}
