package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONArrayTest_from {
    @Test
    public void test_0() {
        assertEquals(0,
                JSONArray.from(new Integer[]{0,1,2,3,4,5}).get(0));
    }

    @Test
    public void test_1() {
        ArrayList<Integer> nums = new ArrayList<>();
        nums.add(0);
        nums.add(1);
        assertEquals(0,
                JSONArray.from(nums).get(0));
    }
}
