package com.alibaba.fastjson2.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;

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
