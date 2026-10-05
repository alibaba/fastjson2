package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StackTypeReviewTest {
    @Test
    public void specializedItemsRetainStackType() {
        TypeReference<Stack<String>> strings = new TypeReference<Stack<String>>() { };
        Stack<String> textStrings = JSON.parseObject("[\"a\",\"b\"]", strings);
        assertEquals("b", textStrings.pop());
        Stack<String> binaryStrings = JSONB.parseObject(JSONB.toBytes(Arrays.asList("a", "b")), strings.getType());
        assertEquals("b", binaryStrings.pop());

        TypeReference<Stack<Long>> longs = new TypeReference<Stack<Long>>() { };
        Stack<Long> textLongs = JSON.parseObject("[1,2]", longs);
        assertEquals(2L, textLongs.pop());
        Stack<Long> binaryLongs = JSONB.parseObject(JSONB.toBytes(Arrays.asList(1L, 2L)), longs.getType());
        assertEquals(2L, binaryLongs.pop());
    }
}
