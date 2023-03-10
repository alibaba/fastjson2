package com.alibaba.fastjson2.read.type;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionTest {
    @Test
    public void arrayList() {
        String str = "[123]";
        ArrayList<String> arrayList = JSON.parseObject(str, new ArrayList<String>() {}.getClass());
        assertEquals("123", arrayList.get(0));
    }

    @Test
    public void linkedList() {
        String str = "[123]";
        LinkedList<String> arrayList = JSON.parseObject(str, new LinkedList<String>() {}.getClass());
        assertEquals("123", arrayList.get(0));
    }
}
