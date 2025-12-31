package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyAccessorFactoryTest {
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
                new PropertyAccessorFactoryUnsafe()
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
            // Test type conversion
            assertEquals(50, accessor.getInt(obj));
            assertEquals(50L, accessor.getLong(obj));
            assertEquals(50.0f, accessor.getFloat(obj), 0.01f);
            assertEquals(50.0, accessor.getDouble(obj), 0.01);
            assertEquals(true, accessor.getBoolean(obj)); // 50 should convert to true
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
            // Test type conversion
            assertEquals(500, accessor.getInt(obj));
            assertEquals(500L, accessor.getLong(obj));
            assertEquals(500.0f, accessor.getFloat(obj), 0.01f);
            assertEquals(500.0, accessor.getDouble(obj), 0.01);
            assertEquals(true, accessor.getBoolean(obj)); // 500 should convert to true
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
            // Test type conversion
            assertEquals(5000L, accessor.getLong(obj));
            assertEquals(5000.0f, accessor.getFloat(obj), 0.01f);
            assertEquals(5000.0, accessor.getDouble(obj), 0.01);
            assertEquals(true, accessor.getBoolean(obj)); // 5000 should convert to true
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
            // Test type conversion
            assertEquals(50000.0f, accessor.getFloat(obj), 0.01f);
            assertEquals(50000.0, accessor.getDouble(obj), 0.01);
            assertEquals(true, accessor.getBoolean(obj)); // 50000 should convert to true
        }
    }

    @ParameterizedTest
    @MethodSource("factoryAndFieldProvider")
    public void testChar(PropertyAccessorFactory factory, Field field) throws Exception {
        if (field.getType() == char.class) {
            PropertyAccessor accessor = factory.create(field);
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setChar(obj, 'A');
            assertEquals('A', accessor.getChar(obj));
            assertEquals(true, accessor.getBoolean(obj)); // non-zero char should convert to true
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
            // Test type conversion
            assertEquals(0, accessor.getInt(obj)); // false should convert to 0
            assertEquals(0L, accessor.getLong(obj)); // false should convert to 0
            assertEquals(0.0f, accessor.getFloat(obj), 0.01f); // false should convert to 0.0f
            assertEquals(0.0, accessor.getDouble(obj), 0.01); // false should convert to 0.0
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
            // Test type conversion
            assertEquals(50.5, accessor.getDouble(obj), 0.01);
            assertEquals(true, accessor.getBoolean(obj)); // non-zero float should convert to true
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
            // Test type conversion
            assertEquals(true, accessor.getBoolean(obj)); // non-zero double should convert to true
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

            // Test with null value
            accessor.setObject(obj, null);
            assertNull(accessor.getObject(obj));
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

            // Test with null array
            accessor.setObject(obj, null);
            assertNull(accessor.getObject(obj));
        }
    }

    @Test
    public void testCreateMethodAccessor() throws Exception {
        // This test is to ensure that the method-based accessor creation works
        // We'll create a PropertyAccessorFactory and verify its basic functionality
        PropertyAccessorFactory factory = new PropertyAccessorFactory();

        // Test that the factory caches accessors
        Field field = TestClass.class.getDeclaredField("intField");
        PropertyAccessor accessor1 = factory.create(field);
        PropertyAccessor accessor2 = factory.create(field);

        // Same field should return same cached accessor
        assertSame(accessor1, accessor2);
    }
}
