package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import kotlin.collections.ArrayDeque;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue793 {
    @Test
    public void test() {
        assertEquals(
                HashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new HashMap() // 指定objectSupplier为 HashMap
                        )
                ).getClass()
        );

        assertEquals(
                LinkedHashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new LinkedHashMap() // 指定objectSupplier为 LinkedHashMap
                        )
                ).getClass()
        );

        assertEquals(
                TreeMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new TreeMap() // 指定objectSupplier为 TreeMap
                        )
                ).getClass()
        );

        assertEquals(
                ConcurrentHashMap.class,
                JSON.parse(
                        "{}",
                        JSONFactory.createReadContext(
                                () -> new ConcurrentHashMap() // 指定objectSupplier为 ConcurrentHashMap
                        )
                ).getClass()
        );
    }

    @Test
    public void test1() {
        assertEquals(
                ArrayList.class,
                JSON.parse(
                        "[]",
                        JSONFactory.createReadContext(
                                null,
                                () -> new ArrayList()) // 指定arraySupplier为 ArrayList
                ).getClass()
        );

        assertEquals(
                LinkedList.class,
                JSON.parse(
                        "[]",
                        JSONFactory.createReadContext(
                                null,
                                () -> new LinkedList()) // 指定arraySupplier为 LinkedList
                ).getClass()
        );

        assertEquals(
                ArrayDeque.class,
                JSON.parse(
                        "[]",
                        JSONFactory.createReadContext(
                                null,
                                () -> new ArrayDeque()) // 指定arraySupplier为 ArrayDeque
                ).getClass()
        );
    }

    @Test
    public void test2() {
        try {
            JSONFactory.setDefaultObjectSupplier(
                    () -> new TreeMap() // 全局指定objectSupplier为 TreeMap
            );
            assertEquals(
                    TreeMap.class,
                    JSON.parse("{}").getClass()
            );

            JSONFactory.setDefaultObjectSupplier(
                    () -> new ConcurrentHashMap() // 全局指定objectSupplier为 ConcurrentHashMap
            );
            assertEquals(
                    ConcurrentHashMap.class,
                    JSON.parse("{}").getClass()
            );

            JSONFactory.setDefaultArraySupplier(
                    () -> new LinkedList() // 全局指定arraySupplier为 LinkedList
            );
            assertEquals(
                    LinkedList.class,
                    JSON.parse("[]").getClass()
            );

            JSONFactory.setDefaultArraySupplier(
                    () -> new CopyOnWriteArrayList() // 全局指定arraySupplier为 CopyOnWriteArrayList
            );
            assertEquals(
                    CopyOnWriteArrayList.class,
                    JSON.parse("[]").getClass()
            );
        } finally {
            JSONFactory.setDefaultObjectSupplier(null);
            JSONFactory.setDefaultArraySupplier(null);

            assertNull(JSONFactory.getDefaultObjectSupplier());
            assertNull(JSONFactory.getDefaultArraySupplier());
        }
    }
}
