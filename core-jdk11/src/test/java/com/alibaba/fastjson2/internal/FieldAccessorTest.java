package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class FieldAccessorTest {
    // A test class with fields of different types
    public static class TestClass {
        private byte byteField = 10;
        private char charField = 'A';
        private short shortField = 100;
        private int intField = 1000;
        private long longField = 10000L;
        private float floatField = 10.5f;
        private double doubleField = 20.7;
        private boolean booleanField = true;
        private String stringField = "test";
        private int[] arrayField = {1, 2, 3};
    }

    static PropertyAccessor[] fieldAccessors() throws Exception {
        TestClass obj = new TestClass();
        Class<?> clazz = obj.getClass();

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());

        Field byteField = clazz.getDeclaredField("byteField");
        Field charField = clazz.getDeclaredField("charField");
        Field shortField = clazz.getDeclaredField("shortField");
        Field intField = clazz.getDeclaredField("intField");
        Field longField = clazz.getDeclaredField("longField");
        Field floatField = clazz.getDeclaredField("floatField");
        Field doubleField = clazz.getDeclaredField("doubleField");
        Field booleanField = clazz.getDeclaredField("booleanField");
        Field stringField = clazz.getDeclaredField("stringField");
        Field arrayField = clazz.getDeclaredField("arrayField");

        return new PropertyAccessor[] {
            new FieldAccessorReflect(byteField),
            new FieldAccessorV(lookup, byteField),
            new FieldAccessorUnsafe(byteField),

            new FieldAccessorReflect(charField),
            new FieldAccessorV(lookup, charField),
            new FieldAccessorUnsafe(charField),

            new FieldAccessorReflect(shortField),
            new FieldAccessorV(lookup, shortField),
            new FieldAccessorUnsafe(shortField),

            new FieldAccessorReflect(intField),
            new FieldAccessorV(lookup, intField),
            new FieldAccessorUnsafe(intField),

            new FieldAccessorReflect(longField),
            new FieldAccessorV(lookup, longField),
            new FieldAccessorUnsafe(longField),

            new FieldAccessorReflect(floatField),
            new FieldAccessorV(lookup, floatField),
            new FieldAccessorUnsafe(floatField),

            new FieldAccessorReflect(doubleField),
            new FieldAccessorV(lookup, doubleField),
            new FieldAccessorUnsafe(doubleField),

            new FieldAccessorReflect(booleanField),
            new FieldAccessorV(lookup, booleanField),
            new FieldAccessorUnsafe(booleanField),

            new FieldAccessorReflect(stringField),
            new FieldAccessorV(lookup, stringField),
            new FieldAccessorUnsafe(stringField),

            new FieldAccessorReflect(arrayField),
            new FieldAccessorV(lookup, arrayField),
            new FieldAccessorUnsafe(arrayField)
        };
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testByte(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == byte.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setByte(obj, (byte) 50);
            assertEquals(50, accessor.getByte(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testShort(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == short.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setShort(obj, (short) 500);
            assertEquals(500, accessor.getShort(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testInt(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == int.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setInt(obj, 5000);
            assertEquals(5000, accessor.getInt(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testLong(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == long.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setLong(obj, 50000L);
            assertEquals(50000L, accessor.getLong(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testChar(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == char.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setChar(obj, 'Z');
            assertEquals('Z', accessor.getChar(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testBoolean(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == boolean.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setBoolean(obj, false);
            assertEquals(false, accessor.getBoolean(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testFloat(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == float.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setFloat(obj, 50.5f);
            assertEquals(50.5f, accessor.getFloat(obj), 0.01f);
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testDouble(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == double.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setDouble(obj, 50.7);
            assertEquals(50.7, accessor.getDouble(obj), 0.01);
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testObject(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == String.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setObject(obj, "modified");
            assertEquals("modified", accessor.getObject(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("fieldAccessors")
    public void testArray(PropertyAccessor accessor) throws Exception {
        Field field = getFieldFromAccessor(accessor);
        if (field.getType() == int[].class) {
            TestClass obj = new TestClass();
            // Test set then get
            int[] newArray = {4, 5, 6};
            accessor.setObject(obj, newArray);
            assertArrayEquals(newArray, (int[]) accessor.getObject(obj));
        }
    }

    @Test
    public void testFieldAccessorVSpecific() throws Exception {
        TestClass obj = new TestClass();
        Field field = TestClass.class.getDeclaredField("intField");

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(TestClass.class, MethodHandles.lookup());

        FieldAccessorV accessor = new FieldAccessorV(lookup, field);

        // Test getting the initial value
        assertEquals(1000, accessor.getInt(obj));

        // Test setting a new value
        accessor.setInt(obj, 2000);
        assertEquals(2000, obj.intField);

        // Test with MethodHandles.Lookup constructor
        FieldAccessorV accessorWithLookup = new FieldAccessorV(lookup, field);
        accessorWithLookup.setInt(obj, 3000);
        assertEquals(3000, obj.intField);
    }

    @Test
    public void testFieldAccessorVExceptionHandling() throws Exception {
        Field field = TestClass.class.getDeclaredField("intField");

        // Create a mock class that would cause issues with VarHandle
        class MockClass {
            private int privateField = 42;
        }

        Field privateField = MockClass.class.getDeclaredField("privateField");
        privateField.setAccessible(true);

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(MockClass.class, MethodHandles.lookup());

        // This should work fine as long as the field is accessible
        FieldAccessorV accessor = new FieldAccessorV(lookup, privateField);
        MockClass obj = new MockClass();

        assertEquals(42, accessor.getInt(obj));
        accessor.setInt(obj, 100);
        assertEquals(100, obj.privateField);
    }

    private Field getFieldFromAccessor(PropertyAccessor accessor) throws NoSuchFieldException {
        // Extract the field from the accessor by accessing its protected field
        try {
            // First try to cast to FieldAccessorReflect to access the field
            if (accessor instanceof FieldAccessor) {
                Field fieldField = FieldAccessor.class.getDeclaredField("field");
                fieldField.setAccessible(true);
                return (Field) fieldField.get(accessor);
            } else {
                throw new RuntimeException("Unknown FieldAccessor implementation: " + accessor.getClass().getName());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Field not found in accessor", e);
        }
    }
}
