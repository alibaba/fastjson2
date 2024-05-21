package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2602 {
    @Test
    public void test() {
        String json = "[{\"a\":998982405},{\"a\":998992165},{\"$ref\":\"$[1]\"}]";
        ArrayList list = JSON.parseObject(json, new TypeReference<ArrayList>() {
        }.getType());
        ConcurrentLinkedQueue queue = JSON.parseObject(json, new TypeReference<ConcurrentLinkedQueue>() {
        }.getType());
        assertEquals(list.get(0), queue.poll());
        assertEquals(list.get(1), queue.poll());
        assertEquals(list.get(2), queue.poll());
    }
}
