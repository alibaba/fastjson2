package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1439 {
    @Test
    public void test() {
        Set<Long> a = new LinkedHashSet<>();
        a.add(null);
        a.add(666L);
        assertEquals("[null,666]", JSON.toJSONString(a));
    }
}
