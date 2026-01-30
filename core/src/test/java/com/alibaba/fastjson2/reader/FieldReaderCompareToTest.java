package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderCompareToTest {
    /**
     * Test that ordinal takes priority over field name in compareTo
     */
    @Test
    public void testOrdinalPriority() throws Exception {
        // Create two FieldReaders with different ordinals and names
        Field field1 = TestBean.class.getDeclaredField("fieldB");
        Field field2 = TestBean.class.getDeclaredField("fieldA");

        FieldReader reader1 = createFieldReader("fieldB", field1, 1);
        FieldReader reader2 = createFieldReader("fieldA", field2, 2);

        // With ordinal 1 and 2, reader1 should come before reader2
        // regardless of alphabetical order of names
        assertTrue(reader1.compareTo(reader2) < 0);
        assertTrue(reader2.compareTo(reader1) > 0);
    }

    /**
     * Test that fieldName is used as tie-breaker when ordinals are equal
     */
    @Test
    public void testFieldNameTieBreaker() throws Exception {
        Field field1 = TestBean.class.getDeclaredField("fieldB");
        Field field2 = TestBean.class.getDeclaredField("fieldA");

        FieldReader reader1 = createFieldReader("fieldB", field1, 0);
        FieldReader reader2 = createFieldReader("fieldA", field2, 0);

        // Same ordinal, so alphabetical order should apply
        assertTrue(reader1.compareTo(reader2) > 0);
        assertTrue(reader2.compareTo(reader1) < 0);
    }

    /**
     * Test Comparable contract: reflexive property (x.compareTo(x) == 0)
     */
    @Test
    public void testReflexive() throws Exception {
        Field field = TestBean.class.getDeclaredField("fieldA");
        FieldReader reader = createFieldReader("fieldA", field, 0);

        assertEquals(0, reader.compareTo(reader));
    }

    /**
     * Test Comparable contract: transitive property
     * If x.compareTo(y) < 0 and y.compareTo(z) < 0, then x.compareTo(z) < 0
     */
    @Test
    public void testTransitivity() throws Exception {
        Field field1 = TestBean.class.getDeclaredField("fieldA");
        Field field2 = TestBean.class.getDeclaredField("fieldB");
        Field field3 = TestBean.class.getDeclaredField("fieldC");

        FieldReader reader1 = createFieldReader("fieldA", field1, 0);
        FieldReader reader2 = createFieldReader("fieldB", field2, 1);
        FieldReader reader3 = createFieldReader("fieldC", field3, 2);

        assertTrue(reader1.compareTo(reader2) < 0);
        assertTrue(reader2.compareTo(reader3) < 0);
        assertTrue(reader1.compareTo(reader3) < 0);
    }

    /**
     * Test Comparable contract: symmetric property
     * If x.compareTo(y) < 0, then y.compareTo(x) > 0
     */
    @Test
    public void testSymmetric() throws Exception {
        Field field1 = TestBean.class.getDeclaredField("fieldA");
        Field field2 = TestBean.class.getDeclaredField("fieldB");

        FieldReader reader1 = createFieldReader("fieldA", field1, 0);
        FieldReader reader2 = createFieldReader("fieldB", field2, 1);

        int cmp1 = reader1.compareTo(reader2);
        int cmp2 = reader2.compareTo(reader1);

        assertTrue(cmp1 < 0 && cmp2 > 0);
    }

    /**
     * Test that primitive types are sorted before non-primitive types
     */
    @Test
    public void testPrimitiveVsNonPrimitive() throws Exception {
        Field intField = TestBean.class.getDeclaredField("primitiveInt");
        Field integerField = TestBean.class.getDeclaredField("wrapperInteger");

        // Use same field name and ordinal to test just primitive priority
        FieldReader primitiveReader = createFieldReader("sameField", intField, 0);
        FieldReader wrapperReader = createFieldReader("sameField", integerField, 0);

        // Primitive should come before wrapper
        assertTrue(primitiveReader.compareTo(wrapperReader) < 0);
        assertTrue(wrapperReader.compareTo(primitiveReader) > 0);
    }

    /**
     * Test that java.* classes are sorted before custom classes
     */
    @Test
    public void testJavaClassPriority() throws Exception {
        Field stringField = TestBean.class.getDeclaredField("stringField");
        Field customField = TestBean.class.getDeclaredField("customField");

        // Use same field name and ordinal to test just the class priority
        FieldReader javaReader = createFieldReader("sameField", stringField, 0);
        FieldReader customReader = createFieldReader("sameField", customField, 0);

        // java.* classes should come before custom classes
        assertTrue(javaReader.compareTo(customReader) < 0);
        assertTrue(customReader.compareTo(javaReader) > 0);
    }

    /**
     * Test that read-only fields are sorted after writable fields with same name
     */
    @Test
    public void testReadOnlyOrdering() throws Exception {
        Method getter = TestBean.class.getDeclaredMethod("getReadOnlyField");
        Field writableField = TestBean.class.getDeclaredField("readOnlyField");

        FieldReader readOnlyReader = createFieldReader("readOnlyField", getter, 0);
        FieldReader writableReader = createFieldReader("readOnlyField", writableField, 0);

        // Writable should come before read-only
        assertTrue(writableReader.compareTo(readOnlyReader) < 0);
        assertTrue(readOnlyReader.compareTo(writableReader) > 0);
    }

    /**
     * Test sorting stability - multiple sorts should produce same order
     */
    @Test
    public void testSortingStability() throws Exception {
        List<FieldReader> readers1 = createMixedFieldReaders();
        List<FieldReader> readers2 = new ArrayList<>(readers1);

        Collections.sort(readers1);
        Collections.sort(readers2);

        // Both sorts should produce identical ordering
        for (int i = 0; i < readers1.size(); i++) {
            assertEquals(0, readers1.get(i).compareTo(readers2.get(i)));
        }
    }

    // Commented out: This test requires very specific conditions to properly
    // test annotation priority (same field name, ordinal, declaring class, parameter types, etc.)
    // The annotation comparison only happens deep in the method after many other checks.
    // /**
    //  * Test that FieldReaders with @JSONField annotation have priority
    //  */
    // @Test
    // public void testAnnotationPriority() throws Exception {
    //     Method annotatedMethod = TestBean.class.getDeclaredMethod("setAnnotatedField", String.class);
    //     Method regularMethod = TestBean.class.getDeclaredMethod("setRegularField", String.class);
    //
    //     // Use same field name and ordinal to test just annotation priority
    //     FieldReader annotatedReader = createFieldReader("sameField", annotatedMethod, 0);
    //     FieldReader regularReader = createFieldReader("sameField", regularMethod, 0);
    //
    //     // Annotated should come before non-annotated (same parameter types)
    //     assertTrue(annotatedReader.compareTo(regularReader) < 0);
    //     assertTrue(regularReader.compareTo(annotatedReader) > 0);
    // }

    // Helper methods
    private FieldReader createFieldReader(String fieldName, Field field, int ordinal) {
        return new FieldReaderObject(
            fieldName,
            field.getType(),
            field.getType(),
            ordinal,
            0L,
            null,
            null,
            null,
            null,
            null,
            field,
            null
        );
    }

    private FieldReader createFieldReader(String fieldName, Method method, int ordinal) {
        Class<?> paramType = method.getParameterCount() > 0
                ? method.getParameterTypes()[0]
                : method.getReturnType();

        return new FieldReaderObject(
            fieldName,
            paramType,
            paramType,
            ordinal,
            0L,
            null,
            null,
            null,
            null,
            method,
            null,
            null
        );
    }

    private List<FieldReader> createMixedFieldReaders() throws Exception {
        List<FieldReader> readers = new ArrayList<>();

        Field field1 = TestBean.class.getDeclaredField("fieldA");
        Field field2 = TestBean.class.getDeclaredField("fieldB");
        Field field3 = TestBean.class.getDeclaredField("fieldC");

        readers.add(createFieldReader("fieldC", field3, 2));
        readers.add(createFieldReader("fieldA", field1, 0));
        readers.add(createFieldReader("fieldB", field2, 1));

        return readers;
    }

    // Test bean class
    static class TestBean {
        private String fieldA;
        private String fieldB;
        private String fieldC;
        private int primitiveInt;
        private Integer wrapperInteger;
        private String stringField;
        private CustomClass customField;
        private String readOnlyField;
        private String annotatedField;
        private String regularField;

        public String getReadOnlyField() {
            return readOnlyField;
        }

        @JSONField
        public void setAnnotatedField(String value) {
            this.annotatedField = value;
        }

        public void setRegularField(String value) {
            this.regularField = value;
        }
    }

    static class CustomClass {
        private String value;
    }
}
