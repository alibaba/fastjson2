package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1657 {
    @Test
    public void test_for_issue() throws Exception {
        HashMap map = JSON.parseObject("\"\"", HashMap.class);
        assertEquals(0, map.size());
    }
}
