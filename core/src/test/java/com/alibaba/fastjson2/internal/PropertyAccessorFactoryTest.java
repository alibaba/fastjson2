package com.alibaba.fastjson2.internal;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

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
        private BigInteger bigIntegerField = new BigInteger("100000");
        private BigDecimal bigDecimalField = new BigDecimal("100000.123");
    }

    public static java.util.Map<Class<?>, Field> fieldMap = new java.util.HashMap<>();
    public static java.util.Map<Class<?>, Method> getters = new java.util.HashMap<>();
    public static java.util.Map<Class<?>, Method> setters = new java.util.HashMap<>();
    static {
        Class<TestClass> clazz = TestClass.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            if (!name.endsWith("Field")) {
                continue;
            }
            Class<?> fieldType = field.getType();
            fieldMap.put(fieldType, field);

            try {
                String getterName;
                if (fieldType == boolean.class) {
                    getterName = "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
                } else {
                    getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                }
                getters.put(fieldType, clazz.getDeclaredMethod(getterName));

                String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                setters.put(fieldType, clazz.getDeclaredMethod(setterName, fieldType));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No such method: " + name);
            }
        }
    }

    static PropertyAccessorFactory[] propertyAccessorFactories() {
        return new PropertyAccessorFactory[] {
                new PropertyAccessorFactory(),
                new PropertyAccessorFactoryUnsafe()
        };
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testByte(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(byte.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        byte value = 50;
        TestClass object = new TestClass();
        propertyAccessor.setByte(object, value);
        assertEquals(value, propertyAccessor.getByte(object));
        assertEquals(value, object.getByteField());

        short shortValue = 51;
        propertyAccessor.setShort(object, shortValue);
        assertEquals(shortValue, propertyAccessor.getShort(object));
        assertEquals((byte) shortValue, object.getByteField());

        int intValue = 52;
        propertyAccessor.setInt(object, intValue);
        assertEquals(intValue, propertyAccessor.getInt(object));
        assertEquals((byte) intValue, object.getByteField());

        // Test long methods
        long longValue = 53L;
        propertyAccessor.setLong(object, longValue);
        assertEquals(longValue, propertyAccessor.getLong(object));
        assertEquals((byte) longValue, object.getByteField());

        // Test float methods
        float floatValue = 54.5f;
        propertyAccessor.setFloat(object, floatValue);
        assertEquals((float) ((byte) floatValue), propertyAccessor.getFloat(object), 0.01f); // 54.5f -> 54 (byte) -> 54.0f
        assertEquals((byte) floatValue, object.getByteField()); // 54.5f -> 54 when cast to byte

        // Test double methods
        double doubleValue = 55.7;
        propertyAccessor.setDouble(object, doubleValue);
        assertEquals((double) ((byte) doubleValue), propertyAccessor.getDouble(object), 0.01); // 55.7 -> 55 (byte) -> 55.0
        assertEquals((byte) doubleValue, object.getByteField()); // 55.7 -> 55 when cast to byte

        // Test char methods
        char charValue = 'X';
        propertyAccessor.setChar(object, charValue);
        assertEquals(charValue, propertyAccessor.getChar(object));
        assertEquals((byte) charValue, object.getByteField());

        // Test boolean methods
        boolean booleanValue = true;
        propertyAccessor.setBoolean(object, booleanValue);
        assertEquals(booleanValue, propertyAccessor.getBoolean(object));
        assertEquals((byte) (booleanValue ? 1 : 0), object.getByteField());

        // Test String methods
        String stringValue = "56";
        propertyAccessor.setString(object, stringValue);
        assertEquals(stringValue, propertyAccessor.getString(object));
        assertEquals((byte) 56, object.getByteField());

        // Test Object methods (setting a byte value as object)
        Byte byteObj = (byte) 57;
        propertyAccessor.setObject(object, byteObj);
        assertEquals(byteObj, propertyAccessor.getObject(object));
        assertEquals(byteObj, object.getByteField());

        // Test BigInteger methods
        BigInteger bigIntegerValue = new BigInteger("58");
        propertyAccessor.setBigInteger(object, bigIntegerValue);
        assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
        assertEquals((byte) 58, object.getByteField());

        // Test BigDecimal methods
        BigDecimal bigDecimalValue = new BigDecimal("59.9");
        propertyAccessor.setBigDecimal(object, bigDecimalValue);
        assertEquals(new BigDecimal((byte) 59), propertyAccessor.getBigDecimal(object)); // 59.9 -> 59 (byte) -> BigDecimal(59)
        assertEquals((byte) 59, object.getByteField());

        // Test Object methods with different types
        propertyAccessor.setObject(object, "60");
        assertEquals((byte) 60, object.getByteField());
        assertEquals("60", propertyAccessor.getString(object));
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testShort(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(short.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        short value = 200;
        TestClass object = new TestClass();
        propertyAccessor.setShort(object, value);
        assertEquals(value, propertyAccessor.getShort(object));
        assertEquals(value, object.getShortField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testInt(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(int.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        int value = 2000;
        TestClass object = new TestClass();
        propertyAccessor.setInt(object, value);
        assertEquals(value, propertyAccessor.getInt(object));
        assertEquals(value, object.getIntField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testLong(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(long.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        long value = 20000L;
        TestClass object = new TestClass();
        propertyAccessor.setLong(object, value);
        assertEquals(value, propertyAccessor.getLong(object));
        assertEquals(value, object.getLongField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testFloat(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(float.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        float value = 30.5f;
        TestClass object = new TestClass();
        propertyAccessor.setFloat(object, value);
        assertEquals(value, propertyAccessor.getFloat(object), 0.01f);
        assertEquals(value, object.getFloatField(), 0.01f);
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testDouble(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(double.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        double value = 40.7;
        TestClass object = new TestClass();
        propertyAccessor.setDouble(object, value);
        assertEquals(value, propertyAccessor.getDouble(object), 0.01);
        assertEquals(value, object.getDoubleField(), 0.01);
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testChar(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(char.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        char value = 'Z';
        TestClass object = new TestClass();
        propertyAccessor.setChar(object, value);
        assertEquals(value, propertyAccessor.getChar(object));
        assertEquals(value, object.getCharField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBoolean(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(boolean.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        boolean value = false;
        TestClass object = new TestClass();
        propertyAccessor.setBoolean(object, value);
        assertEquals(value, propertyAccessor.getBoolean(object));
        assertEquals(value, object.isBooleanField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testString(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(String.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        String value = "updated";
        TestClass object = new TestClass();
        propertyAccessor.setString(object, value);
        assertEquals(value, propertyAccessor.getString(object));
        assertEquals(value, object.getStringField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBigInteger(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(BigInteger.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        BigInteger value = new BigInteger("200000");
        TestClass object = new TestClass();
        propertyAccessor.setBigInteger(object, value);
        assertEquals(value, propertyAccessor.getBigInteger(object));
        assertEquals(value, object.getBigIntegerField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBigDecimal(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(BigDecimal.class);

        PropertyAccessor propertyAccessor = factory.create(field);

        BigDecimal value = new BigDecimal("200000.456");
        TestClass object = new TestClass();
        propertyAccessor.setBigDecimal(object, value);
        assertEquals(value, propertyAccessor.getBigDecimal(object));
        assertEquals(value, object.getBigDecimalField());
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testIntArray(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(int[].class);

        PropertyAccessor propertyAccessor = factory.create(field);

        int[] value = {4, 5, 6};
        TestClass object = new TestClass();
        propertyAccessor.setObject(object, value);
        assertArrayEquals(value, (int[]) propertyAccessor.getObject(object));
        assertArrayEquals(value, object.getArrayField());
    }
}
