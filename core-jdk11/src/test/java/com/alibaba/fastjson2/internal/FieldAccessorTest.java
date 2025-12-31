package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.stream.Stream;

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

    static Stream<Arguments> factoryAndFieldProvider() throws Exception {
        TestClass obj = new TestClass();
        Class<?> clazz = obj.getClass();

        Field[] fields = clazz.getDeclaredFields();
        PropertyAccessorFactory[] factories = new PropertyAccessorFactory[] {
                new PropertyAccessorFactory(),
                new PropertyAccessorFactoryUnsafe(),
                new PropertyAccessorFactoryV(
                        MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                )
        };

        Arguments[] arguments = new Arguments[fields.length * factories.length];

        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < factories.length; j++) {
                arguments[i * factories.length + j] = Arguments.of(factories[j], fields[i]);
            }
        }

        return Stream.of(arguments);
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testByte(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == byte.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setByte(obj, (byte) 50);
            assertEquals(50, accessor.getByte(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testShort(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == short.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setShort(obj, (short) 500);
            assertEquals(500, accessor.getShort(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testInt(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == int.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setInt(obj, 5000);
            assertEquals(5000, accessor.getInt(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testLong(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == long.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setLong(obj, 50000L);
            assertEquals(50000L, accessor.getLong(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testChar(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == char.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setChar(obj, 'Z');
            assertEquals('Z', accessor.getChar(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testBoolean(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == boolean.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setBoolean(obj, false);
            assertEquals(false, accessor.getBoolean(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testFloat(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == float.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setFloat(obj, 50.5f);
            assertEquals(50.5f, accessor.getFloat(obj), 0.01f);
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testDouble(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == double.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setDouble(obj, 50.7);
            assertEquals(50.7, accessor.getDouble(obj), 0.01);
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testObject(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == String.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setObject(obj, "modified");
            assertEquals("modified", accessor.getObject(obj));
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testArray(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == int[].class) {
            PropertyAccessor accessor = factory.create(field);
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
}
