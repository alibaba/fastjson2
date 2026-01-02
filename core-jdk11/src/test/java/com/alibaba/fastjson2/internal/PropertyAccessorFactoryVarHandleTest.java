package com.alibaba.fastjson2.internal;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyAccessorFactoryVarHandleTest
{
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
                new PropertyAccessorFactoryUnsafe(),
                new PropertyAccessorFactoryVarHandle()
        };
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testByte(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(byte.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
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
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testShort(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(short.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            short value = 200;
            TestClass object = new TestClass();
            propertyAccessor.setShort(object, value);
            assertEquals(value, propertyAccessor.getShort(object));
            assertEquals(value, object.getShortField());

            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((short) byteValue, object.getShortField());

            int intValue = 250;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals((short) intValue, object.getShortField());

            // Test long methods
            long longValue = 251L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((short) longValue, object.getShortField());

            // Test float methods
            float floatValue = 252.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((short) floatValue), propertyAccessor.getFloat(object), 0.01f); // 252.5f -> 252 (short) -> 252.0f
            assertEquals((short) floatValue, object.getShortField()); // 252.5f -> 252 when cast to short

            // Test double methods
            double doubleValue = 253.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) ((short) doubleValue), propertyAccessor.getDouble(object), 0.01); // 253.7 -> 253 (short) -> 253.0
            assertEquals((short) doubleValue, object.getShortField()); // 253.7 -> 253 when cast to short

            // Test char methods
            char charValue = 'Y';
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals((short) charValue, object.getShortField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals((short) (booleanValue ? 1 : 0), object.getShortField());

            // Test String methods
            String stringValue = "254";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals((short) 254, object.getShortField());

            // Test Object methods (setting a short value as object)
            Short shortObj = (short) 255;
            propertyAccessor.setObject(object, shortObj);
            assertEquals(shortObj, propertyAccessor.getObject(object));
            assertEquals(shortObj, object.getShortField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("256");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((short) 256, object.getShortField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("257.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal((short) 257), propertyAccessor.getBigDecimal(object)); // 257.9 -> 257 (short) -> BigDecimal(257)
            assertEquals((short) 257, object.getShortField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "258");
            assertEquals((short) 258, object.getShortField());
            assertEquals("258", propertyAccessor.getString(object));
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testInt(PropertyAccessorFactory factory) throws Exception
    {
        Field field = fieldMap.get(int.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            int value = 2000;
            TestClass object = new TestClass();
            propertyAccessor.setInt(object, value);
            assertEquals(value, propertyAccessor.getInt(object));
            assertEquals(value, object.getIntField());

            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((int) byteValue, object.getIntField());

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((int) shortValue, object.getIntField());

            // Test long methods
            long longValue = 2001L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((int) longValue, object.getIntField());

            // Test float methods
            float floatValue = 2002.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((int) floatValue), propertyAccessor.getFloat(object), 0.01f); // 2002.5f -> 2002 (int) -> 2002.0f
            assertEquals((int) floatValue, object.getIntField()); // 2002.5f -> 2002 when cast to int

            // Test double methods
            double doubleValue = 2003.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) ((int) doubleValue), propertyAccessor.getDouble(object), 0.01); // 2003.7 -> 2003 (int) -> 2003.0
            assertEquals((int) doubleValue, object.getIntField()); // 2003.7 -> 2003 when cast to int

            // Test char methods
            char charValue = 'Z';
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals((int) charValue, object.getIntField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals((int) (booleanValue ? 1 : 0), object.getIntField());

            // Test String methods
            String stringValue = "2004";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals((int) 2004, object.getIntField());

            // Test Object methods (setting an int value as object)
            Integer intObj = 2005;
            propertyAccessor.setObject(object, intObj);
            assertEquals(intObj, propertyAccessor.getObject(object));
            assertEquals(intObj, object.getIntField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("2006");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((int) 2006, object.getIntField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("2007.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal((int) 2007), propertyAccessor.getBigDecimal(object)); // 2007.9 -> 2007 (int) -> BigDecimal(2007)
            assertEquals((int) 2007, object.getIntField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "2008");
            assertEquals((int) 2008, object.getIntField());
            assertEquals("2008", propertyAccessor.getString(object));
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testLong(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(long.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            long value = 20000L;
            TestClass object = new TestClass();
            propertyAccessor.setLong(object, value);
            assertEquals(value, propertyAccessor.getLong(object));
            assertEquals(value, object.getLongField());

            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((long) byteValue, object.getLongField());

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((long) shortValue, object.getLongField());

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals((long) intValue, object.getLongField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((long) floatValue), propertyAccessor.getFloat(object), 0.01f); // 20001.5f -> 20001 (long) -> 20001.0f
            assertEquals((long) floatValue, object.getLongField()); // 20001.5f -> 20001 when cast to long

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) ((long) doubleValue), propertyAccessor.getDouble(object), 0.01); // 20002.7 -> 20002 (long) -> 20002.0
            assertEquals((long) doubleValue, object.getLongField()); // 20002.7 -> 20002 when cast to long

            // Test char methods
            char charValue = 'A';
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals((long) charValue, object.getLongField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals((long) (booleanValue ? 1 : 0), object.getLongField());

            // Test String methods
            String stringValue = "20003";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals((long) 20003, object.getLongField());

            // Test Object methods (setting a long value as object)
            Long longObj = 20004L;
            propertyAccessor.setObject(object, longObj);
            assertEquals(longObj, propertyAccessor.getObject(object));
            assertEquals(longObj, object.getLongField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20005");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((long) 20005, object.getLongField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20006.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal((long) 20006), propertyAccessor.getBigDecimal(object)); // 20006.9 -> 20006 (long) -> BigDecimal(20006)
            assertEquals((long) 20006, object.getLongField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20007");
            assertEquals((long) 20007, object.getLongField());
            assertEquals("20007", propertyAccessor.getString(object));
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testFloat(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(float.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            float value = 30.5f;
            TestClass object = new TestClass();
            propertyAccessor.setFloat(object, value);
            assertEquals(value, propertyAccessor.getFloat(object), 0.01f);
            assertEquals(value, object.getFloatField(), 0.01f);

            // Test Object methods (setting a float value as object)
            Float floatObj = 20003.5f;
            propertyAccessor.setObject(object, floatObj);
            assertEquals(floatObj, propertyAccessor.getObject(object));
            assertEquals(floatObj, object.getFloatField(), 0.01f);

            // Test String methods
            String stringValue = "20002";
            propertyAccessor.setString(object, stringValue);
            assertEquals("20002.0", propertyAccessor.getString(object));
            assertEquals((float) 20002, object.getFloatField(), 0.01f);

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals((float) 20006, object.getFloatField(), 0.01f);
            assertEquals("20006.0", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to float
            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((float) byteValue, object.getFloatField(), 0.01f);

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((float) shortValue, object.getFloatField(), 0.01f);

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals((float) intValue, object.getFloatField(), 0.01f);

            long longValue = 20000L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((float) longValue, object.getFloatField(), 0.01f);

            // Test double methods
            double doubleValue = 20001.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) ((float) doubleValue), propertyAccessor.getDouble(object), 0.01); // 20001.7 -> 20001 (float) -> 20001.0
            assertEquals((float) doubleValue, object.getFloatField(), 0.01f); // 20001.7 -> 20001 when cast to float

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals((float) (booleanValue ? 1 : 0), object.getFloatField(), 0.01f);

            // Test char methods
            char charValue = 'A'; // Use a character that can be converted to a number
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals((float) charValue, object.getFloatField(), 0.01f);

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20004");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((float) 20004, object.getFloatField(), 0.01f);

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal(bigDecimalValue.floatValue()), propertyAccessor.getBigDecimal(object)); // 20005.9 -> 20005 (float) -> BigDecimal(20005)
            assertEquals(bigDecimalValue.floatValue(), object.getFloatField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testDouble(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(double.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            double value = 40.7;
            TestClass object = new TestClass();
            propertyAccessor.setDouble(object, value);
            assertEquals(value, propertyAccessor.getDouble(object), 0.01);
            assertEquals(value, object.getDoubleField(), 0.01);

            // Test Object methods (setting a double value as object)
            Double doubleObj = 20003.7;
            propertyAccessor.setObject(object, doubleObj);
            assertEquals(doubleObj, propertyAccessor.getObject(object));
            assertEquals(doubleObj, object.getDoubleField(), 0.01);

            // Test String methods
            String stringValue = "20002";
            propertyAccessor.setString(object, stringValue);
            assertEquals("20002.0", propertyAccessor.getString(object));
            assertEquals((double) 20002, object.getDoubleField(), 0.01);

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals((double) 20006, object.getDoubleField(), 0.01);
            assertEquals("20006.0", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to double
            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((double) byteValue, object.getDoubleField(), 0.01);

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((double) shortValue, object.getDoubleField(), 0.01);

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals((double) intValue, object.getDoubleField(), 0.01);

            long longValue = 20000L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((double) longValue, object.getDoubleField(), 0.01);

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((double) floatValue), propertyAccessor.getFloat(object), 0.01f); // 20001.5f -> 20001 (double) -> 20001.0f
            assertEquals((double) floatValue, object.getDoubleField(), 0.01); // 20001.5f -> 20001 when cast to double

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals((double) (booleanValue ? 1 : 0), object.getDoubleField(), 0.01);

            // Test char methods
            char charValue = '3'; // Use a character that can be converted to a number
            propertyAccessor.setChar(object, charValue);
            assertEquals((char) 3, propertyAccessor.getChar(object));
            assertEquals((double) 3, object.getDoubleField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20004");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((double) 20004, object.getDoubleField(), 0.01);

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(BigDecimal.valueOf(bigDecimalValue.doubleValue()), propertyAccessor.getBigDecimal(object)); // 20005.9 -> 20005 (double) -> BigDecimal(20005)
            assertEquals(bigDecimalValue.doubleValue(), object.getDoubleField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testChar(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(char.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            char value = 'Z';
            TestClass object = new TestClass();
            propertyAccessor.setChar(object, value);
            assertEquals(value, propertyAccessor.getChar(object));
            assertEquals(value, object.getCharField());

            // Test Object methods (setting a char value as object)
            Character charObj = 'H';
            propertyAccessor.setObject(object, charObj);
            assertEquals(charObj, propertyAccessor.getObject(object));
            assertEquals(charObj, object.getCharField());

            // Test String methods
            String stringValue = "G";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals('G', object.getCharField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "K");
            assertEquals('K', object.getCharField());
            assertEquals("K", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to char
            byte byteValue = 65; // 'A' in ASCII
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((char) byteValue, object.getCharField());

            short shortValue = 66; // 'B' in ASCII
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((char) shortValue, object.getCharField());

            int intValue = '3'; // 'C' in ASCII
            propertyAccessor.setInt(object, intValue);
            assertEquals(3, propertyAccessor.getInt(object));
            assertEquals((char) intValue, object.getCharField());

            long longValue = 68L; // 'D' in ASCII
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((char) longValue, object.getCharField());

            // Test float methods
            float floatValue = 69.0f; // 'E' in ASCII
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((char) floatValue), propertyAccessor.getFloat(object), 0.01f); // 69.0f -> 69 (char) -> 69.0f
            assertEquals((char) floatValue, object.getCharField()); // 69.0f -> 69 when cast to char

            // Test double methods
            double doubleValue = '6'; // 'F' in ASCII
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals(6, propertyAccessor.getDouble(object), 0.01); // 70.0 -> 70 (char) -> 70.0
            assertEquals('6', object.getCharField()); // 70.0 -> 70 when cast to char

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals('1', object.getCharField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("7"); // 'I' in ASCII
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals((char) bigIntegerValue.intValue(), object.getCharField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("4"); // 'J' in ASCII
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal(4), propertyAccessor.getBigDecimal(object)); // 74 -> 74 (char) -> BigDecimal(74)
            assertEquals((char) bigDecimalValue.intValue(), object.getCharField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBoolean(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(boolean.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            boolean value = false;
            TestClass object = new TestClass();
            propertyAccessor.setBoolean(object, value);
            assertEquals(value, propertyAccessor.getBoolean(object));
            assertEquals(value, object.isBooleanField());

            // Test Object methods (setting a boolean value as object)
            Boolean boolObj = false;
            propertyAccessor.setObject(object, boolObj);
            assertEquals(boolObj, propertyAccessor.getObject(object));
            assertEquals(boolObj, object.isBooleanField());

            // Test String methods
            String stringValue = "true";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals(true, object.isBooleanField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "false");
            assertEquals(false, object.isBooleanField());
            assertEquals("false", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to boolean
            byte byteValue = 1; // true when converted to boolean
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals((boolean) (byteValue != 0), object.isBooleanField());

            short shortValue = 0; // false when converted to boolean
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals((boolean) (shortValue != 0), object.isBooleanField());

            int intValue = 1; // true when converted to boolean
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals((boolean) (intValue != 0), object.isBooleanField());

            long longValue = 0L; // false when converted to boolean
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals((boolean) (longValue != 0), object.isBooleanField());

            // Test float methods
            float floatValue = 1.0f; // true when converted to boolean
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) ((boolean) (floatValue != 0) ? 1 : 0), propertyAccessor.getFloat(object), 0.01f);
            assertEquals((boolean) (floatValue != 0), object.isBooleanField());

            // Test double methods
            double doubleValue = 0.0; // false when converted to boolean
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) ((boolean) (doubleValue != 0) ? 1 : 0), propertyAccessor.getDouble(object), 0.01);
            assertEquals((boolean) (doubleValue != 0), object.isBooleanField());

            // Test char methods
            char charValue = '1'; // true when converted to boolean (non-zero)
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals(true, object.isBooleanField());

            char charValue2 = '0'; // false when converted to boolean (zero)
            propertyAccessor.setChar(object, charValue2);
            assertEquals(charValue2, propertyAccessor.getChar(object));
            assertEquals(false, object.isBooleanField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("1"); // true when converted to boolean
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals(true, object.isBooleanField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("0.0"); // false when converted to boolean
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal((boolean) (bigDecimalValue.compareTo(BigDecimal.ZERO) != 0) ? 1 : 0), propertyAccessor.getBigDecimal(object));
            assertEquals(false, object.isBooleanField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testString(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(String.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            String value = "updated";
            TestClass object = new TestClass();
            propertyAccessor.setString(object, value);
            assertEquals(value, propertyAccessor.getString(object));
            assertEquals(value, object.getStringField());

            // Test Object methods (setting a string value as object)
            String stringObj = "test";
            propertyAccessor.setObject(object, stringObj);
            assertEquals(stringObj, propertyAccessor.getObject(object));
            assertEquals(stringObj, object.getStringField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, 123);
            assertEquals("123", object.getStringField());
            assertEquals("123", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to string
            byte byteValue = 3; // 'A' in ASCII
            propertyAccessor.setByte(object, byteValue);
            assertEquals(3, propertyAccessor.getByte(object));
            assertEquals("3", object.getStringField());

            short shortValue = 4; // 'B' in ASCII
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals("4", object.getStringField());

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals(String.valueOf(intValue), object.getStringField());

            long longValue = 20000L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals(String.valueOf(longValue), object.getStringField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) Float.parseFloat(String.valueOf(floatValue)), propertyAccessor.getFloat(object), 0.01f);
            assertEquals(String.valueOf(floatValue), object.getStringField());

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) Double.parseDouble(String.valueOf(doubleValue)), propertyAccessor.getDouble(object), 0.01);
            assertEquals(String.valueOf(doubleValue), object.getStringField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals(String.valueOf(booleanValue), object.getStringField());

            // Test char methods
            char charValue = 'Z';
            propertyAccessor.setChar(object, charValue);
            assertEquals(charValue, propertyAccessor.getChar(object));
            assertEquals(String.valueOf(charValue), object.getStringField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20003");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals(String.valueOf(bigIntegerValue), object.getStringField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20004.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(bigDecimalValue, propertyAccessor.getBigDecimal(object));
            assertEquals(String.valueOf(bigDecimalValue), object.getStringField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBigInteger(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(BigInteger.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            BigInteger value = new BigInteger("200000");
            TestClass object = new TestClass();
            propertyAccessor.setBigInteger(object, value);
            assertEquals(value, propertyAccessor.getBigInteger(object));
            assertEquals(value, object.getBigIntegerField());

            // Test Object methods (setting a BigInteger value as object)
            BigInteger bigIntObj = new BigInteger("20004");
            propertyAccessor.setObject(object, bigIntObj);
            assertEquals(bigIntObj, propertyAccessor.getObject(object));
            assertEquals(bigIntObj, object.getBigIntegerField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setBigDecimal(object, bigDecimalValue);
            assertEquals(new BigDecimal(BigInteger.valueOf(20005)), propertyAccessor.getBigDecimal(object)); // 20005.9 -> 20005 as integer part
            assertEquals(BigInteger.valueOf(20005), object.getBigIntegerField());

            // Test String methods
            String stringValue = "20003";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals(new BigInteger("20003"), object.getBigIntegerField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals(new BigInteger("20006"), object.getBigIntegerField());
            assertEquals("20006", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to BigInteger
            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals(BigInteger.valueOf(byteValue), object.getBigIntegerField());

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals(BigInteger.valueOf(shortValue), object.getBigIntegerField());

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals(BigInteger.valueOf(intValue), object.getBigIntegerField());

            long longValue = 20000L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals(BigInteger.valueOf(longValue), object.getBigIntegerField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) BigInteger.valueOf((long) floatValue).floatValue(), propertyAccessor.getFloat(object), 0.01f);
            assertEquals(BigInteger.valueOf((long) floatValue), object.getBigIntegerField()); // 20001.5f -> 20001 as integer part

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) BigInteger.valueOf((long) doubleValue).doubleValue(), propertyAccessor.getDouble(object), 0.01);
            assertEquals(BigInteger.valueOf((long) doubleValue), object.getBigIntegerField()); // 20002.7 -> 20002 as integer part

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals(BigInteger.valueOf(booleanValue ? 1 : 0), object.getBigIntegerField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBigDecimal(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(BigDecimal.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            BigDecimal value = new BigDecimal("200000.456");
            TestClass object = new TestClass();
            propertyAccessor.setBigDecimal(object, value);
            assertEquals(value, propertyAccessor.getBigDecimal(object));
            assertEquals(value, object.getBigDecimalField());

            // Test Object methods (setting a BigDecimal value as object)
            BigDecimal bigDecObj = new BigDecimal("20004.9");
            propertyAccessor.setObject(object, bigDecObj);
            assertEquals(bigDecObj, propertyAccessor.getObject(object));
            assertEquals(bigDecObj, object.getBigDecimalField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20005");
            propertyAccessor.setBigInteger(object, bigIntegerValue);
            assertEquals(bigIntegerValue, propertyAccessor.getBigInteger(object));
            assertEquals(new BigDecimal(bigIntegerValue), object.getBigDecimalField());

            // Test String methods
            String stringValue = "20003.8";
            propertyAccessor.setString(object, stringValue);
            assertEquals(stringValue, propertyAccessor.getString(object));
            assertEquals(new BigDecimal("20003.8"), object.getBigDecimalField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006.1");
            assertEquals(new BigDecimal("20006.1"), object.getBigDecimalField());
            assertEquals("20006.1", propertyAccessor.getString(object));

            // Test primitive setters that can be converted to BigDecimal
            byte byteValue = 50;
            propertyAccessor.setByte(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByte(object));
            assertEquals(BigDecimal.valueOf(byteValue), object.getBigDecimalField());

            short shortValue = 200;
            propertyAccessor.setShort(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShort(object));
            assertEquals(BigDecimal.valueOf(shortValue), object.getBigDecimalField());

            int intValue = 2000;
            propertyAccessor.setInt(object, intValue);
            assertEquals(intValue, propertyAccessor.getInt(object));
            assertEquals(BigDecimal.valueOf(intValue), object.getBigDecimalField());

            long longValue = 20000L;
            propertyAccessor.setLong(object, longValue);
            assertEquals(longValue, propertyAccessor.getLong(object));
            assertEquals(BigDecimal.valueOf(longValue), object.getBigDecimalField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloat(object, floatValue);
            assertEquals((float) BigDecimal.valueOf(floatValue).floatValue(), propertyAccessor.getFloat(object), 0.01f);
            assertEquals(BigDecimal.valueOf(floatValue), object.getBigDecimalField());

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDouble(object, doubleValue);
            assertEquals((double) BigDecimal.valueOf(doubleValue).doubleValue(), propertyAccessor.getDouble(object), 0.01);
            assertEquals(BigDecimal.valueOf(doubleValue), object.getBigDecimalField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBoolean(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBoolean(object));
            assertEquals(BigDecimal.valueOf(booleanValue ? 1 : 0), object.getBigDecimalField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testIntArray(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(int[].class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            int[] value = {4, 5, 6};
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertArrayEquals(value, (int[]) propertyAccessor.getObject(object));
            assertArrayEquals(value, object.getArrayField());

            // Test with Object methods (setting the array as object)
            int[] testArray = {13, 14, 15};
            propertyAccessor.setObject(object, testArray);
            assertArrayEquals(testArray, (int[]) propertyAccessor.getObject(object));
            assertArrayEquals(testArray, object.getArrayField());
        }
    }
}
