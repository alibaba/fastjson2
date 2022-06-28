package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue513 {
    @Test
    public void test() {
        Set set = (Set) JSON.parse("Set[\"1541357098843803649\"]");
        assertEquals(1, set.size());
        assertTrue(set.contains("1541357098843803649"));
    }
}
