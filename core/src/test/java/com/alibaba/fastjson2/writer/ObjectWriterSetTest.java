package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectWriterSetTest {
    @Test
    public void testJsonbSet() {
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        set.add(null);
        String str = JSON.toJSONString(set);
        assertEquals(str, "[null,1]");
    }
}
