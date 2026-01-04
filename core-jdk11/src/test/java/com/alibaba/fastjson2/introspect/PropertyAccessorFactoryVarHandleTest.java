package com.alibaba.fastjson2.introspect;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyAccessorFactoryVarHandleTest {
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

        // Wrapper type fields
        private Byte byteObjField = 10;
        private Character charObjField = 'A';
        private Short shortObjField = 100;
        private Integer intObjField = 1000;
        private Long longObjField = 10000L;
        private Float floatObjField = 10.5f;
        private Double doubleObjField = 20.7;
        private Boolean booleanObjField = true;

        public TestClass() {
        }

        public TestClass(int intField) {
            this.intField = intField;
        }

        public TestClass(long longField) {
            this.longField = longField;
        }

        public TestClass(double doubleField) {
            this.doubleField = doubleField;
        }

        public TestClass(Byte byteObjField) {
            this.byteObjField = byteObjField;
        }
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
                if (fieldType == boolean.class || fieldType == Boolean.class) {
                    if (name.startsWith("is")) {
                        getterName = name;
                    } else {
                        getterName = (fieldType == boolean.class) ? "is" + name.substring(0, 1).toUpperCase() + name.substring(1)
                                : "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
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
                new PropertyAccessorFactoryVarHandle(),
                new PropertyAccessorFactoryMethodHandle()
        };
    }

    /**
     * Tests the createSupplier method of PropertyAccessorFactory.
     * This test verifies that the factory can create a Supplier that successfully
     * instantiates objects using the given constructor.
     *
     * @param factory the PropertyAccessorFactory to test
     * @throws Exception if an error occurs during the test
     */
    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void allocate(PropertyAccessorFactory factory) throws Exception {
        assertNotNull(factory.createSupplier(TestClass.class.getDeclaredConstructor())
                .get());

        assertEquals((byte) 50, ((TestClass) factory.createFunction(TestClass.class.getDeclaredConstructor(Byte.class)).apply((byte) 50)).byteObjField);
        assertEquals(1001, ((TestClass) factory.createIntFunction(TestClass.class.getDeclaredConstructor(int.class)).apply(1001)).intField);
        assertEquals(1002L, ((TestClass) factory.createLongFunction(TestClass.class.getDeclaredConstructor(long.class)).apply(1002L)).longField);
        assertEquals(123.45D, ((TestClass) factory.createDoubleFunction(TestClass.class.getDeclaredConstructor(double.class)).apply(123.45D)).doubleField);
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
            propertyAccessor.setByteValue(object, value);
            assertEquals(value, propertyAccessor.getByteValue(object));
            assertEquals(value, object.getByteField());

            short shortValue = 51;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((byte) shortValue, object.getByteField());

            int intValue = 52;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((byte) intValue, object.getByteField());

            // Test long methods
            long longValue = 53L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((byte) longValue, object.getByteField());

            // Test float methods
            float floatValue = 54.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((byte) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 54.5f -> 54 (byte) -> 54.0f
            assertEquals((byte) floatValue, object.getByteField()); // 54.5f -> 54 when cast to byte

            // Test double methods
            double doubleValue = 55.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((byte) doubleValue), propertyAccessor.getDoubleValue(object), 0.01); // 55.7 -> 55 (byte) -> 55.0
            assertEquals((byte) doubleValue, object.getByteField()); // 55.7 -> 55 when cast to byte

            // Test char methods
            char charValue = 'X';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals((byte) charValue, object.getByteField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((byte) (booleanValue ? 1 : 0), object.getByteField());

            // Test String methods
            String stringValue = "56";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Byte.parseByte(stringValue), propertyAccessor.getObject(object));
            assertEquals((byte) 56, object.getByteField());

            // Test Object methods (setting a byte value as object)
            Byte byteObj = (byte) 57;
            propertyAccessor.setObject(object, byteObj);
            assertEquals(byteObj, propertyAccessor.getObject(object));
            assertEquals(byteObj, object.getByteField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("58");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.byteValue(), propertyAccessor.getObject(object));
            assertEquals((byte) 58, object.getByteField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("59.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.byteValue(), propertyAccessor.getObject(object)); // 59.9 -> 59 (byte) -> BigDecimal(59)
            assertEquals((byte) 59, object.getByteField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "60");
            assertEquals((byte) 60, object.getByteField());
            assertEquals((byte) 60, propertyAccessor.getObject(object));
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
            propertyAccessor.setShortValue(object, value);
            assertEquals(value, propertyAccessor.getShortValue(object));
            assertEquals(value, object.getShortField());

            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((short) byteValue, object.getShortField());

            int intValue = 250;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((short) intValue, object.getShortField());

            // Test long methods
            long longValue = 251L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((short) longValue, object.getShortField());

            // Test float methods
            float floatValue = 252.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((short) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 252.5f -> 252 (short) -> 252.0f
            assertEquals((short) floatValue, object.getShortField()); // 252.5f -> 252 when cast to short

            // Test double methods
            double doubleValue = 253.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((short) doubleValue), propertyAccessor.getDoubleValue(object), 0.01); // 253.7 -> 253 (short) -> 253.0
            assertEquals((short) doubleValue, object.getShortField()); // 253.7 -> 253 when cast to short

            // Test char methods
            char charValue = 'Y';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals((short) charValue, object.getShortField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((short) (booleanValue ? 1 : 0), object.getShortField());

            // Test String methods
            String stringValue = "254";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Short.parseShort(stringValue), propertyAccessor.getObject(object));
            assertEquals((short) 254, object.getShortField());

            // Test Object methods (setting a short value as object)
            Short shortObj = (short) 255;
            propertyAccessor.setObject(object, shortObj);
            assertEquals(shortObj, propertyAccessor.getObject(object));
            assertEquals(shortObj, object.getShortField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("256");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.shortValue(), propertyAccessor.getObject(object));
            assertEquals((short) 256, object.getShortField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("257.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.shortValue(), propertyAccessor.getObject(object)); // 257.9 -> 257 (short) -> BigDecimal(257)
            assertEquals((short) 257, object.getShortField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "258");
            assertEquals((short) 258, object.getShortField());
            assertEquals((short) 258, propertyAccessor.getObject(object));
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
            propertyAccessor.setIntValue(object, value);
            assertEquals(value, propertyAccessor.getIntValue(object));
            assertEquals(value, object.getIntField());

            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((int) byteValue, object.getIntField());

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((int) shortValue, object.getIntField());

            // Test long methods
            long longValue = 2001L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((int) longValue, object.getIntField());

            // Test float methods
            float floatValue = 2002.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((int) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 2002.5f -> 2002 (int) -> 2002.0f
            assertEquals((int) floatValue, object.getIntField()); // 2002.5f -> 2002 when cast to int

            // Test double methods
            double doubleValue = 2003.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((int) doubleValue), propertyAccessor.getDoubleValue(object), 0.01); // 2003.7 -> 2003 (int) -> 2003.0
            assertEquals((int) doubleValue, object.getIntField()); // 2003.7 -> 2003 when cast to int

            // Test char methods
            char charValue = 'Z';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals((int) charValue, object.getIntField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((int) (booleanValue ? 1 : 0), object.getIntField());

            // Test String methods
            String stringValue = "2004";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Integer.parseInt(stringValue), propertyAccessor.getObject(object));
            assertEquals((int) 2004, object.getIntField());

            // Test Object methods (setting an int value as object)
            Integer intObj = 2005;
            propertyAccessor.setObject(object, intObj);
            assertEquals(intObj, propertyAccessor.getObject(object));
            assertEquals(intObj, object.getIntField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("2006");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.intValue(), propertyAccessor.getObject(object));
            assertEquals((int) 2006, object.getIntField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("2007.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.intValue(), propertyAccessor.getObject(object)); // 2007.9 -> 2007 (int) -> BigDecimal(2007)
            assertEquals((int) 2007, object.getIntField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "2008");
            assertEquals((int) 2008, object.getIntField());
            assertEquals(2008, propertyAccessor.getObject(object));
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
            propertyAccessor.setLongValue(object, value);
            assertEquals(value, propertyAccessor.getLongValue(object));
            assertEquals(value, object.getLongField());

            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((long) byteValue, object.getLongField());

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((long) shortValue, object.getLongField());

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((long) intValue, object.getLongField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((long) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 20001.5f -> 20001 (long) -> 20001.0f
            assertEquals((long) floatValue, object.getLongField()); // 20001.5f -> 20001 when cast to long

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((long) doubleValue), propertyAccessor.getDoubleValue(object), 0.01); // 20002.7 -> 20002 (long) -> 20002.0
            assertEquals((long) doubleValue, object.getLongField()); // 20002.7 -> 20002 when cast to long

            // Test char methods
            char charValue = 'A';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals((long) charValue, object.getLongField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((long) (booleanValue ? 1 : 0), object.getLongField());

            // Test String methods
            String stringValue = "20003";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Long.parseLong(stringValue), propertyAccessor.getObject(object));
            assertEquals((long) 20003, object.getLongField());

            // Test Object methods (setting a long value as object)
            Long longObj = 20004L;
            propertyAccessor.setObject(object, longObj);
            assertEquals(longObj, propertyAccessor.getObject(object));
            assertEquals(longObj, object.getLongField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20005");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.longValue(), propertyAccessor.getObject(object));
            assertEquals((long) 20005, object.getLongField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20006.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.longValue(), propertyAccessor.getObject(object)); // 20006.9 -> 20006 (long) -> BigDecimal(20006)
            assertEquals((long) 20006, object.getLongField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20007");
            assertEquals((long) 20007, object.getLongField());
            assertEquals(20007L, propertyAccessor.getObject(object));
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
            propertyAccessor.setFloatValue(object, value);
            assertEquals(value, propertyAccessor.getFloatValue(object), 0.01f);
            assertEquals(value, object.getFloatField(), 0.01f);

            // Test Object methods (setting a float value as object)
            Float floatObj = 20003.5f;
            propertyAccessor.setObject(object, floatObj);
            assertEquals(floatObj, propertyAccessor.getObject(object));
            assertEquals(floatObj, object.getFloatField(), 0.01f);

            // Test String methods
            String stringValue = "20002";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Float.parseFloat(stringValue), propertyAccessor.getObject(object));
            assertEquals((float) 20002, object.getFloatField(), 0.01f);

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals((float) 20006, object.getFloatField(), 0.01f);
            assertEquals((float) 20006.0, propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to float
            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((float) byteValue, object.getFloatField(), 0.01f);

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((float) shortValue, object.getFloatField(), 0.01f);

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((float) intValue, object.getFloatField(), 0.01f);

            long longValue = 20000L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((float) longValue, object.getFloatField(), 0.01f);

            // Test double methods
            double doubleValue = 20001.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((float) doubleValue), propertyAccessor.getDoubleValue(object), 0.01); // 20001.7 -> 20001 (float) -> 20001.0
            assertEquals((float) doubleValue, object.getFloatField(), 0.01f); // 20001.7 -> 20001 when cast to float

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((float) (booleanValue ? 1 : 0), object.getFloatField(), 0.01f);

            // Test char methods
            char charValue = 'A'; // Use a character that can be converted to a number
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals((float) charValue, object.getFloatField(), 0.01f);

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20004");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.floatValue(), propertyAccessor.getObject(object));
            assertEquals((float) 20004, object.getFloatField(), 0.01f);

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.floatValue(), propertyAccessor.getObject(object)); // 20005.9 -> 20005 (float) -> BigDecimal(20005)
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
            propertyAccessor.setDoubleValue(object, value);
            assertEquals(value, propertyAccessor.getDoubleValue(object), 0.01);
            assertEquals(value, object.getDoubleField(), 0.01);

            // Test Object methods (setting a double value as object)
            Double doubleObj = 20003.7;
            propertyAccessor.setObject(object, doubleObj);
            assertEquals(doubleObj, propertyAccessor.getObject(object));
            assertEquals(doubleObj, object.getDoubleField(), 0.01);

            // Test String methods
            String stringValue = "20002";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Double.parseDouble(stringValue), propertyAccessor.getObject(object));
            assertEquals((double) 20002, object.getDoubleField(), 0.01);

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals((double) 20006, object.getDoubleField(), 0.01);
            assertEquals((double) 20006.0, propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to double
            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((double) byteValue, object.getDoubleField(), 0.01);

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((double) shortValue, object.getDoubleField(), 0.01);

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((double) intValue, object.getDoubleField(), 0.01);

            long longValue = 20000L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((double) longValue, object.getDoubleField(), 0.01);

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((double) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 20001.5f -> 20001 (double) -> 20001.0f
            assertEquals((double) floatValue, object.getDoubleField(), 0.01); // 20001.5f -> 20001 when cast to double

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals((double) (booleanValue ? 1 : 0), object.getDoubleField(), 0.01);

            // Test char methods
            char charValue = '3'; // Use a character that can be converted to a number
            propertyAccessor.setCharValue(object, charValue);
            assertEquals((char) 3, propertyAccessor.getCharValue(object));
            assertEquals((double) 3, object.getDoubleField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20004");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.doubleValue(), propertyAccessor.getObject(object));
            assertEquals((double) 20004, object.getDoubleField(), 0.01);

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.doubleValue(), propertyAccessor.getObject(object)); // 20005.9 -> 20005 (double) -> BigDecimal(20005)
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
            propertyAccessor.setCharValue(object, value);
            assertEquals(value, propertyAccessor.getCharValue(object));
            assertEquals(value, object.getCharField());

            // Test Object methods (setting a char value as object)
            Character charObj = 'H';
            propertyAccessor.setObject(object, charObj);
            assertEquals(charObj, propertyAccessor.getObject(object));
            assertEquals(charObj, object.getCharField());

            // Test String methods
            String stringValue = "G";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(stringValue.charAt(0), propertyAccessor.getObject(object));
            assertEquals('G', object.getCharField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "K");
            assertEquals('K', object.getCharField());
            assertEquals('K', propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to char
            byte byteValue = 65; // 'A' in ASCII
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((char) byteValue, object.getCharField());

            short shortValue = 66; // 'B' in ASCII
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((char) shortValue, object.getCharField());

            int intValue = '3'; // 'C' in ASCII
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(3, propertyAccessor.getIntValue(object));
            assertEquals((char) intValue, object.getCharField());

            long longValue = 68L; // 'D' in ASCII
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((char) longValue, object.getCharField());

            // Test float methods
            float floatValue = 69.0f; // 'E' in ASCII
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((char) floatValue), propertyAccessor.getFloatValue(object), 0.01f); // 69.0f -> 69 (char) -> 69.0f
            assertEquals((char) floatValue, object.getCharField()); // 69.0f -> 69 when cast to char

            // Test double methods
            double doubleValue = '6'; // 'F' in ASCII
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals(6, propertyAccessor.getDoubleValue(object), 0.01); // 70.0 -> 70 (char) -> 70.0
            assertEquals('6', object.getCharField()); // 70.0 -> 70 when cast to char

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals('1', object.getCharField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("7"); // 'I' in ASCII
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals((char) bigIntegerValue.intValue(), propertyAccessor.getObject(object));
            assertEquals((char) bigIntegerValue.intValue(), object.getCharField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("4"); // 'J' in ASCII
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals((char) bigDecimalValue.intValue(), propertyAccessor.getObject(object)); // 74 -> 74 (char) -> BigDecimal(74)
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
            propertyAccessor.setBooleanValue(object, value);
            assertEquals(value, propertyAccessor.getBooleanValue(object));
            assertEquals(value, object.isBooleanField());

            // Test Object methods (setting a boolean value as object)
            Boolean boolObj = false;
            propertyAccessor.setObject(object, boolObj);
            assertEquals(boolObj, propertyAccessor.getObject(object));
            assertEquals(boolObj, object.isBooleanField());

            // Test String methods
            String stringValue = "true";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Boolean.parseBoolean(stringValue), propertyAccessor.getObject(object));
            assertEquals(true, object.isBooleanField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "false");
            assertEquals(false, object.isBooleanField());
            assertEquals(false, propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to boolean
            byte byteValue = 1; // true when converted to boolean
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals((boolean) (byteValue != 0), object.isBooleanField());

            short shortValue = 0; // false when converted to boolean
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals((boolean) (shortValue != 0), object.isBooleanField());

            int intValue = 1; // true when converted to boolean
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals((boolean) (intValue != 0), object.isBooleanField());

            long longValue = 0L; // false when converted to boolean
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals((boolean) (longValue != 0), object.isBooleanField());

            // Test float methods
            float floatValue = 1.0f; // true when converted to boolean
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) ((boolean) (floatValue != 0) ? 1 : 0), propertyAccessor.getFloatValue(object), 0.01f);
            assertEquals((boolean) (floatValue != 0), object.isBooleanField());

            // Test double methods
            double doubleValue = 0.0; // false when converted to boolean
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) ((boolean) (doubleValue != 0) ? 1 : 0), propertyAccessor.getDoubleValue(object), 0.01);
            assertEquals((boolean) (doubleValue != 0), object.isBooleanField());

            // Test char methods
            char charValue = '1'; // true when converted to boolean (non-zero)
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals(true, object.isBooleanField());

            char charValue2 = '0'; // false when converted to boolean (zero)
            propertyAccessor.setCharValue(object, charValue2);
            assertEquals(charValue2, propertyAccessor.getCharValue(object));
            assertEquals(false, object.isBooleanField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("1"); // true when converted to boolean
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(bigIntegerValue.intValue() != 0, propertyAccessor.getObject(object));
            assertEquals(true, object.isBooleanField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("0.0"); // false when converted to boolean
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.compareTo(BigDecimal.ZERO) != 0, propertyAccessor.getObject(object));
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
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getStringField());

            // Test Object methods (setting a string value as object)
            String stringObj = "test";
            propertyAccessor.setObject(object, stringObj);
            assertEquals(stringObj, propertyAccessor.getObject(object));
            assertEquals(stringObj, object.getStringField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, 123);
            assertEquals("123", object.getStringField());
            assertEquals("123", propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to string
            byte byteValue = 3; // 'A' in ASCII
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(3, propertyAccessor.getByteValue(object));
            assertEquals("3", object.getStringField());

            short shortValue = 4; // 'B' in ASCII
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals("4", object.getStringField());

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals(String.valueOf(intValue), object.getStringField());

            long longValue = 20000L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals(String.valueOf(longValue), object.getStringField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) Float.parseFloat(String.valueOf(floatValue)), propertyAccessor.getFloatValue(object), 0.01f);
            assertEquals(String.valueOf(floatValue), object.getStringField());

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) Double.parseDouble(String.valueOf(doubleValue)), propertyAccessor.getDoubleValue(object), 0.01);
            assertEquals(String.valueOf(doubleValue), object.getStringField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
            assertEquals(String.valueOf(booleanValue), object.getStringField());

            // Test char methods
            char charValue = 'Z';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(charValue, propertyAccessor.getCharValue(object));
            assertEquals(String.valueOf(charValue), object.getStringField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20003");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(String.valueOf(bigIntegerValue), propertyAccessor.getObject(object));
            assertEquals(String.valueOf(bigIntegerValue), object.getStringField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20004.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(String.valueOf(bigDecimalValue), propertyAccessor.getObject(object));
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
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getBigIntegerField());

            // Test Object methods (setting a BigInteger value as object)
            BigInteger bigIntObj = new BigInteger("20004");
            propertyAccessor.setObject(object, bigIntObj);
            assertEquals(bigIntObj, propertyAccessor.getObject(object));
            assertEquals(bigIntObj, object.getBigIntegerField());

            // Test BigDecimal methods
            BigDecimal bigDecimalValue = new BigDecimal("20005.9");
            propertyAccessor.setObject(object, bigDecimalValue);
            assertEquals(bigDecimalValue.toBigInteger(), propertyAccessor.getObject(object)); // 20005.9 -> 20005 as integer part
            assertEquals(BigInteger.valueOf(20005), object.getBigIntegerField());

            // Test String methods
            String stringValue = "20003";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(new BigInteger(stringValue), propertyAccessor.getObject(object));
            assertEquals(new BigInteger("20003"), object.getBigIntegerField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006");
            assertEquals(new BigInteger("20006"), object.getBigIntegerField());
            assertEquals(new BigInteger("20006"), propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to BigInteger
            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals(BigInteger.valueOf(byteValue), object.getBigIntegerField());

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals(BigInteger.valueOf(shortValue), object.getBigIntegerField());

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals(BigInteger.valueOf(intValue), object.getBigIntegerField());

            long longValue = 20000L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals(BigInteger.valueOf(longValue), object.getBigIntegerField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) BigInteger.valueOf((long) floatValue).floatValue(), propertyAccessor.getFloatValue(object), 0.01f);
            assertEquals(BigInteger.valueOf((long) floatValue), object.getBigIntegerField()); // 20001.5f -> 20001 as integer part

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) BigInteger.valueOf((long) doubleValue).doubleValue(), propertyAccessor.getDoubleValue(object), 0.01);
            assertEquals(BigInteger.valueOf((long) doubleValue), object.getBigIntegerField()); // 20002.7 -> 20002 as integer part

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
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
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getBigDecimalField());

            // Test Object methods (setting a BigDecimal value as object)
            BigDecimal bigDecObj = new BigDecimal("20004.9");
            propertyAccessor.setObject(object, bigDecObj);
            assertEquals(bigDecObj, propertyAccessor.getObject(object));
            assertEquals(bigDecObj, object.getBigDecimalField());

            // Test BigInteger methods
            BigInteger bigIntegerValue = new BigInteger("20005");
            propertyAccessor.setObject(object, bigIntegerValue);
            assertEquals(new BigDecimal(bigIntegerValue), propertyAccessor.getObject(object));
            assertEquals(new BigDecimal(bigIntegerValue), object.getBigDecimalField());

            // Test String methods
            String stringValue = "20003.8";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(new BigDecimal(stringValue), propertyAccessor.getObject(object));
            assertEquals(new BigDecimal("20003.8"), object.getBigDecimalField());

            // Test Object methods with different types
            propertyAccessor.setObject(object, "20006.1");
            assertEquals(new BigDecimal("20006.1"), object.getBigDecimalField());
            assertEquals(new BigDecimal("20006.1"), propertyAccessor.getObject(object));

            // Test primitive setters that can be converted to BigDecimal
            byte byteValue = 50;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(byteValue, propertyAccessor.getByteValue(object));
            assertEquals(BigDecimal.valueOf(byteValue), object.getBigDecimalField());

            short shortValue = 200;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(shortValue, propertyAccessor.getShortValue(object));
            assertEquals(BigDecimal.valueOf(shortValue), object.getBigDecimalField());

            int intValue = 2000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(intValue, propertyAccessor.getIntValue(object));
            assertEquals(BigDecimal.valueOf(intValue), object.getBigDecimalField());

            long longValue = 20000L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(longValue, propertyAccessor.getLongValue(object));
            assertEquals(BigDecimal.valueOf(longValue), object.getBigDecimalField());

            // Test float methods
            float floatValue = 20001.5f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals((float) BigDecimal.valueOf(floatValue).floatValue(), propertyAccessor.getFloatValue(object), 0.01f);
            assertEquals(BigDecimal.valueOf(floatValue), object.getBigDecimalField());

            // Test double methods
            double doubleValue = 20002.7;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals((double) BigDecimal.valueOf(doubleValue).doubleValue(), propertyAccessor.getDoubleValue(object), 0.01);
            assertEquals(BigDecimal.valueOf(doubleValue), object.getBigDecimalField());

            // Test boolean methods
            boolean booleanValue = true;
            propertyAccessor.setBooleanValue(object, booleanValue);
            assertEquals(booleanValue, propertyAccessor.getBooleanValue(object));
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

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testByteObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Byte.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Byte value = 50;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getByteObjField());

            // Test String methods
            String stringValue = "57";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Byte.valueOf((byte) 57), propertyAccessor.getObject(object));
            assertEquals(Byte.valueOf((byte) 57), object.getByteObjField());

            // Test primitive setters that can be converted to Byte
            byte byteValue = 51;
            propertyAccessor.setByteValue(object, byteValue);
            assertEquals(Byte.valueOf(byteValue), propertyAccessor.getObject(object));
            assertEquals(Byte.valueOf(byteValue), object.getByteObjField());

            // Test with boolean (0 or 1) - using small values that fit in byte range
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Byte.valueOf((byte) (boolValue ? 1 : 0)), propertyAccessor.getObject(object));
            assertEquals(Byte.valueOf((byte) (boolValue ? 1 : 0)), object.getByteObjField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testShortObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Short.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Short value = 200;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getShortObjField());

            // Test String methods
            String stringValue = "254";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Short.valueOf((short) 254), propertyAccessor.getObject(object));
            assertEquals(Short.valueOf((short) 254), object.getShortObjField());

            // Test primitive setters that can be converted to Short
            short shortValue = 201;
            propertyAccessor.setShortValue(object, shortValue);
            assertEquals(Short.valueOf(shortValue), propertyAccessor.getObject(object));
            assertEquals(Short.valueOf(shortValue), object.getShortObjField());

            // Test with integer values in short range
            int intValue = 10000;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(Short.valueOf((short) intValue), propertyAccessor.getObject(object));
            assertEquals(Short.valueOf((short) intValue), object.getShortObjField());

            // Test with boolean (0 or 1)
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Short.valueOf((short) (boolValue ? 1 : 0)), propertyAccessor.getObject(object));
            assertEquals(Short.valueOf((short) (boolValue ? 1 : 0)), object.getShortObjField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testIntegerObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Integer.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Integer value = 2000;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getIntObjField());

            // Test String methods
            String stringValue = "2005";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Integer.valueOf((int) 2005), propertyAccessor.getObject(object));
            assertEquals(Integer.valueOf((int) 2005), object.getIntObjField());

            // Test primitive setters that can be converted to Integer
            int intValue = 2001;
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(Integer.valueOf(intValue), propertyAccessor.getObject(object));
            assertEquals(Integer.valueOf(intValue), object.getIntObjField());

            // Test with boolean (0 or 1)
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Integer.valueOf((boolValue ? 1 : 0)), propertyAccessor.getObject(object));
            assertEquals(Integer.valueOf((boolValue ? 1 : 0)), object.getIntObjField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testLongObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Long.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Long value = 20000L;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getLongObjField());

            // Test String methods
            String stringValue = "20004";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Long.parseLong(stringValue), propertyAccessor.getObject(object));
            assertEquals(Long.valueOf((long) 20004), object.getLongObjField());

            // Test primitive setters that can be converted to Long
            long longValue = 20001L;
            propertyAccessor.setLongValue(object, longValue);
            assertEquals(Long.valueOf(longValue), propertyAccessor.getObject(object));
            assertEquals(Long.valueOf(longValue), object.getLongObjField());

            // Test with boolean (0 or 1)
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Long.valueOf((boolValue ? 1L : 0L)), propertyAccessor.getObject(object));
            assertEquals(Long.valueOf((boolValue ? 1L : 0L)), object.getLongObjField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testFloatObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Float.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Float value = 30.5f;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getFloatObjField(), 0.01f);

            // Test String methods
            String stringValue = "31.7";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Float.valueOf(Float.parseFloat(stringValue)), propertyAccessor.getObject(object));
            assertEquals(Float.valueOf(Float.parseFloat(stringValue)), object.getFloatObjField(), 0.01f);

            // Test primitive setters that can be converted to Float
            float floatValue = 45.6f;
            propertyAccessor.setFloatValue(object, floatValue);
            assertEquals(Float.valueOf(floatValue), propertyAccessor.getObject(object));
            assertEquals(Float.valueOf(floatValue), object.getFloatObjField(), 0.01f);

            // Test with boolean (0 or 1)
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Float.valueOf((boolValue ? 1.0f : 0.0f)), propertyAccessor.getObject(object));
            assertEquals(Float.valueOf((boolValue ? 1.0f : 0.0f)), object.getFloatObjField(), 0.01f);
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testDoubleObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Double.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Double value = 40.7;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getDoubleObjField(), 0.01);

            // Test String methods
            String stringValue = "45.8";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Double.parseDouble(stringValue), propertyAccessor.getObject(object));
            assertEquals(Double.valueOf(Double.parseDouble(stringValue)), object.getDoubleObjField(), 0.01);

            // Test primitive setters that can be converted to Double
            double doubleValue = 55.9;
            propertyAccessor.setDoubleValue(object, doubleValue);
            assertEquals(Double.valueOf(doubleValue), propertyAccessor.getObject(object));
            assertEquals(Double.valueOf(doubleValue), object.getDoubleObjField(), 0.01);

            // Test with boolean (0 or 1)
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Double.valueOf((boolValue ? 1.0 : 0.0)), propertyAccessor.getObject(object));
            assertEquals(Double.valueOf((boolValue ? 1.0 : 0.0)), object.getDoubleObjField(), 0.01);
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testCharacterObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Character.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Character value = 'Z';
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getCharObjField());

            // Test String methods
            String stringValue = "G";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(stringValue.charAt(0), propertyAccessor.getObject(object));
            assertEquals(Character.valueOf('G'), object.getCharObjField());

            // Test primitive setters that can be converted to Character
            char charValue = 'H';
            propertyAccessor.setCharValue(object, charValue);
            assertEquals(Character.valueOf(charValue), propertyAccessor.getObject(object));
            assertEquals(Character.valueOf(charValue), object.getCharObjField());

            // Test with integer (ASCII value)
            int intValue = 65; // 'A' in ASCII
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(Character.valueOf((char) intValue), propertyAccessor.getObject(object));
            assertEquals(Character.valueOf((char) intValue), object.getCharObjField());
        }
    }

    @ParameterizedTest
    @MethodSource("propertyAccessorFactories")
    public void testBooleanObj(PropertyAccessorFactory factory) throws Exception {
        Field field = fieldMap.get(Boolean.class);
        Class<?> fieldType = field.getType();
        PropertyAccessor[] propertyAccessors = {
                factory.create(field),
                factory.create(field.getName(), fieldType, null, getters.get(fieldType), setters.get(fieldType))
        };
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            Boolean value = false;
            TestClass object = new TestClass();
            propertyAccessor.setObject(object, value);
            assertEquals(value, propertyAccessor.getObject(object));
            assertEquals(value, object.getBooleanObjField());

            // Test String methods
            String stringValue = "true";
            propertyAccessor.setObject(object, stringValue);
            assertEquals(Boolean.parseBoolean(stringValue), propertyAccessor.getObject(object));
            assertEquals(Boolean.valueOf(true), object.getBooleanObjField());

            // Test primitive setters that can be converted to Boolean
            boolean boolValue = true;
            propertyAccessor.setBooleanValue(object, boolValue);
            assertEquals(Boolean.valueOf(boolValue), propertyAccessor.getObject(object));
            assertEquals(Boolean.valueOf(boolValue), object.getBooleanObjField());

            // Test with integer (0 or 1)
            int intValue = 0; // false
            propertyAccessor.setIntValue(object, intValue);
            assertEquals(Boolean.valueOf(intValue != 0), propertyAccessor.getObject(object));
            assertEquals(Boolean.valueOf(intValue != 0), object.getBooleanObjField());
        }
    }
}
