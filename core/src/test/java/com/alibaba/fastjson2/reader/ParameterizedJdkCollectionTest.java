package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParameterizedJdkCollectionTest {
    static Stream<Class<? extends Collection>> collectionTypes() {
        return Stream.of(
                ArrayDeque.class,
                PriorityQueue.class,
                Vector.class,
                LinkedBlockingDeque.class,
                LinkedBlockingQueue.class,
                LinkedTransferQueue.class,
                PriorityBlockingQueue.class,
                CopyOnWriteArraySet.class
        );
    }

    @ParameterizedTest
    @MethodSource("collectionTypes")
    public void parseText(Class<? extends Collection> collectionClass) {
        Type type = new ParameterizedTypeImpl(new Type[]{Long.class}, null, collectionClass);
        assertLongValues(JSON.parseObject("[1,2147483648,-1]", type), collectionClass);
    }

    @ParameterizedTest
    @MethodSource("collectionTypes")
    public void parseJSONB(Class<? extends Collection> collectionClass) {
        Type type = new ParameterizedTypeImpl(new Type[]{Long.class}, null, collectionClass);
        byte[] jsonb = JSONB.toBytes(Arrays.asList(1L, 2147483648L, -1L));
        assertLongValues(JSONB.parseObject(jsonb, type), collectionClass);
    }

    private static void assertLongValues(
            Collection<Long> values,
            Class<? extends Collection> collectionClass
    ) {
        assertEquals(collectionClass, values.getClass());
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(1L, 2147483648L, -1L)));
        assertTrue(values.stream().allMatch(value -> value instanceof Long));
    }
}
