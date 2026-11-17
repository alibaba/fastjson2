package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityHashCollisionTest {
    public static class Parent {
        public List<Child> items = new ArrayList<>();
    }

    public static class Child {
        public List<String> first;
        public List<String> second;
    }

    private enum Example { VALUE }

    @Test
    public void hashCollisionDoesNotExcludeMessageClass() {
        // A controlled false hash match reproduces a collision without depending on a JVM's hash allocation.
        int[] primitive = ObjectWriterProvider.PRIMITIVE_HASH_CODES;
        int[] excluded = ObjectWriterProvider.NOT_REFERENCES_TYPE_HASH_CODES;
        int[] primitiveBackup = primitive.clone();
        int[] excludedBackup = excluded.clone();
        try {
            int replacedHash = System.identityHashCode(java.util.Currency.class);
            primitive[Arrays.binarySearch(primitive, replacedHash)] = System.identityHashCode(Child.class);
            excluded[Arrays.binarySearch(excluded, replacedHash)] = System.identityHashCode(Child.class);
            Arrays.sort(primitive);
            Arrays.sort(excluded);
            assertFalse(ObjectWriterProvider.isPrimitiveOrEnum(Child.class));
            assertFalse(ObjectWriterProvider.isNotReferenceDetect(Child.class));
            assertTrue(ObjectWriterProvider.isPrimitiveOrEnum(String.class));
            assertTrue(ObjectWriterProvider.isNotReferenceDetect(int[].class));
            assertTrue(ObjectWriterProvider.isNotReferenceDetect(Example.class));

            List<String> shared = new ArrayList<>();
            Parent parent = new Parent();
            for (int i = 0; i < 2; i++) {
                Child child = new Child();
                child.first = shared;
                child.second = shared;
                parent.items.add(child);
            }
            byte[] bytes = JSONB.toBytes(parent, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);
            Parent result = JSONB.parseObject(bytes, Parent.class, JSONReader.Feature.FieldBased);
            assertNotNull(result.items.get(0).first);
            assertSame(result.items.get(0).first, result.items.get(0).second);
            assertSame(result.items.get(0).first, result.items.get(1).first);
            assertSame(result.items.get(0).first, result.items.get(1).second);
        } finally {
            System.arraycopy(primitiveBackup, 0, primitive, 0, primitive.length);
            System.arraycopy(excludedBackup, 0, excluded, 0, excluded.length);
        }
    }
}
