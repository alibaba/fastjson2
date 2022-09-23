package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue793 {
    @Test
    public void test() {
        assertEquals(
                HashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new HashMap()) // 指定objectSupplier为 HashMap
                ).getClass()
        );

        assertEquals(
                LinkedHashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new LinkedHashMap()) // 指定objectSupplier为 LinkedHashMap
                ).getClass()
        );

        assertEquals(
                TreeMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new TreeMap()) // 指定objectSupplier为 TreeMap
                ).getClass()
        );

        assertEquals(
                ConcurrentHashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new ConcurrentHashMap()) // 指定objectSupplier为 ConcurrentHashMap
                ).getClass()
        );
    }
}
