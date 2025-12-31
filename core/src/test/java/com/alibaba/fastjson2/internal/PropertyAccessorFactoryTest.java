package com.alibaba.fastjson2.internal;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyAccessorFactoryTest {
    // A test class with fields of different types
    @Getter
    @Setter
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

    static PropertyAccessor[] propertyAccessor() throws Exception {
        TestClass obj = new TestClass();
        Class<?> clazz = obj.getClass();

        Field[] fields = clazz.getDeclaredFields();

        PropertyAccessorFactory[] factories = new PropertyAccessorFactory[] {
                new PropertyAccessorFactory(),
                new PropertyAccessorFactoryUnsafe()
        };

        PropertyAccessor[] propertyAccessors = new PropertyAccessor[fields.length * factories.length * 2];

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[0];
            String fieldName = field.getName();
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method getter = clazz.getDeclaredMethod(getterName);
            Method setter = clazz.getDeclaredMethod(setterName, field.getType());

            for (int j = 0; j < factories.length; j++) {
                int index = (i * factories.length + j) * 2;
                propertyAccessors[index] = factories[j].create(field);
                propertyAccessors[index + 1] = factories[j].create(fieldName, null, null, getter, setter);
            }
        }

        return propertyAccessors;
    }

    @ParameterizedTest
    @MethodSource("propertyAccessor")
    public void testByte(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == byte.class) {
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
    @MethodSource("propertyAccessor")
    public void testShort(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == short.class) {
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
    @MethodSource("propertyAccessor")
    public void testInt(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == int.class) {
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
    @MethodSource("propertyAccessor")
    public void testLong(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == long.class) {
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
    @MethodSource("propertyAccessor")
    public void testChar(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == char.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setChar(obj, 'A');
            assertEquals('A', accessor.getChar(obj));
            assertEquals(true, accessor.getBoolean(obj)); // non-zero char should convert to true
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessor")
    public void testBoolean(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == boolean.class) {
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
    @MethodSource("propertyAccessor")
    public void testFloat(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == float.class) {
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
    @MethodSource("propertyAccessor")
    public void testDouble(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == double.class) {
            TestClass obj = new TestClass();
            // Test set then get
            accessor.setDouble(obj, 50.7);
            assertEquals(50.7, accessor.getDouble(obj), 0.01);
            // Test type conversion
            assertEquals(true, accessor.getBoolean(obj)); // non-zero double should convert to true
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessor")
    public void testObject(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == String.class) {
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
    @MethodSource("propertyAccessor")
    public void testArray(PropertyAccessor accessor) throws Exception {
        if (accessor.propertyClass() == int[].class) {
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
}
