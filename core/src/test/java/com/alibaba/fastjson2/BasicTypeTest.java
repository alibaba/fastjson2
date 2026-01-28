package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONReader.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

public class BasicTypeTest {
    public static Class<?>[] types() {
        return new Class<?>[] {
            // byte/Byte tests
            ByteF.class, ByteM.class, ByteC.class,
            ByteF1.class, ByteF2.class,
            ByteValueF.class, ByteValueM.class, ByteValueC.class,
            ByteM1.class, ByteM2.class,

            // short/Short tests
            ShortF.class, ShortM.class, ShortC.class,
            ShortF1.class, ShortF2.class,
            ShortValueF.class, ShortValueM.class, ShortValueC.class,
            ShortM1.class, ShortM2.class,

            // int/Integer tests
            IntF.class, IntM.class, IntC.class,
            IntegerF1.class, IntegerF2.class,
            IntValueF.class, IntValueM.class, IntValueC.class,
            IntegerM1.class, IntegerM2.class,

            // long/Long tests
            LongF.class, LongM.class, LongC.class,
            LongF1.class, LongF2.class,
            LongValueF.class, LongValueM.class, LongValueC.class,
            LongM1.class, LongM2.class,

            // BigInteger tests
            BigIntegerF.class, BigIntegerM.class, BigIntegerC.class,
            BigIntegerF1.class, BigIntegerF2.class,
            BigIntegerM1.class, BigIntegerM2.class,

            // BigDecimal tests
            BigDecimalF.class, BigDecimalM.class, BigDecimalC.class,
            BigDecimalF1.class, BigDecimalF2.class,
            BigDecimalM1.class, BigDecimalM2.class,

            // String tests
            StringF.class, StringM.class, StringC.class,
            StringF1.class, StringF2.class,
            StringM1.class, StringM2.class,

            // UUID tests
            UUIDF.class, UUIDM.class, UUIDC.class,
            UUIDF1.class, UUIDF2.class,
            UUIDM1.class, UUIDM2.class,

            // Number tests
            NumberF.class, NumberM.class, NumberC.class,

            // float/Float tests
            FloatF.class, FloatM.class, FloatC.class,
            FloatF1.class, FloatF2.class,
            FloatValueF.class, FloatValueM.class, FloatValueC.class,
            FloatM1.class, FloatM2.class,

            // double/Double tests
            DoubleF.class, DoubleM.class, DoubleC.class,
            DoubleF1.class, DoubleF2.class,
            DoubleValueF.class, DoubleValueM.class, DoubleValueC.class,
            DoubleM1.class, DoubleM2.class,

            // boolean/Boolean tests
            BooleanF.class, BooleanM.class, BooleanC.class,
            BooleanF1.class, BooleanF2.class,
            BooleanM1.class, BooleanM2.class,
            BooleanValueF.class, BooleanValueM.class, BooleanValueC.class,

            // char/Character tests
            CharacterF.class, CharacterM.class, CharacterC.class,
            CharacterF1.class, CharacterF2.class,
            CharacterM1.class, CharacterM2.class,
            CharValueF.class, CharValueM.class, CharValueC.class,

            // Array tests
            ByteArrayF.class, ByteArrayM.class, ByteArrayC.class, ShortArrayF.class, ShortArrayM.class, ShortArrayC.class,
            IntArrayF.class, IntArrayM.class, IntArrayC.class,
            LongArrayF.class, LongArrayM.class, LongArrayC.class, FloatArrayF.class, FloatArrayM.class, FloatArrayC.class,
            DoubleArrayF.class, DoubleArrayM.class, DoubleArrayC.class,
            BooleanArrayF.class, BooleanArrayM.class, BooleanArrayC.class, CharArrayF.class, CharArrayM.class, CharArrayC.class,
            StringArrayF.class, StringArrayM.class, StringArrayC.class,

            // Collection/List tests
            CollectionStringF.class, CollectionStringM.class, CollectionStringC.class,
            ListStringF.class, ListStringM.class, ListStringC.class,

            // ArrayList and LinkedList tests
            ArrayListStringF.class, ArrayListStringM.class, ArrayListStringC.class,
            LinkedListStringF.class, LinkedListStringM.class, LinkedListStringC.class,

            // Set tests
            HashSetStringF.class, HashSetStringM.class, HashSetStringC.class,
            LinkedHashSetStringF.class, LinkedHashSetStringM.class, LinkedHashSetStringC.class,

            // Map tests
            HashMapStringF.class, HashMapStringM.class, HashMapStringC.class,
            LinkedHashMapStringF.class, LinkedHashMapStringM.class, LinkedHashMapStringC.class,

            // List<Number> tests
            ListByteF.class, ListByteM.class, ListByteC.class,
            ListShortF.class, ListShortM.class, ListShortC.class,
            ListIntegerF.class, ListIntegerM.class, ListIntegerC.class,
            ListLongF.class, ListLongM.class, ListLongC.class,
            ListBigIntegerF.class, ListBigIntegerM.class, ListBigIntegerC.class,
            ListBigDecimalF.class, ListBigDecimalM.class, ListBigDecimalC.class,
            ListNumberF.class, ListNumberM.class, ListNumberC.class
        };
    }

    static Object[] values(Class<?> type, java.lang.reflect.Type genericType) {
        if (type == java.util.List.class && genericType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pType = (java.lang.reflect.ParameterizedType) genericType;
            java.lang.reflect.Type[] typeArgs = pType.getActualTypeArguments();
            if (typeArgs.length == 1 && typeArgs[0] instanceof Class) {
                Class<?> elementType = (Class<?>) typeArgs[0];
                if (elementType == Byte.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList((byte) 0, (byte) 1, (byte) -1, Byte.MAX_VALUE, Byte.MIN_VALUE)};
                }
                if (elementType == Short.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList((short) 0, (short) 1, (short) -1, Short.MAX_VALUE, Short.MIN_VALUE)};
                }
                if (elementType == Integer.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList(0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE)};
                }
                if (elementType == Long.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList(0L, 1L, -1L, Long.MAX_VALUE)};
                }
                if (elementType == BigInteger.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN,
                            BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MIN_VALUE))};
                }
                if (elementType == BigDecimal.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN,
                            new BigDecimal("123.456"), new BigDecimal("-789.012"))};
                }
                if (elementType == Number.class) {
                    return new Object[] {null, java.util.Collections.emptyList(),
                        java.util.Arrays.asList(0, 1L, 1.0, BigInteger.ONE, BigDecimal.ONE)};
                }
            }
        }
        if (type == byte.class) {
            return new Byte[] {0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE};
        }
        if (type == Byte.class) {
            return new Byte[] {null, 0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE};
        }
        if (type == short.class) {
            return new Short[] {0, 1, -1, Short.MAX_VALUE, Short.MIN_VALUE};
        }
        if (type == Short.class) {
            return new Short[] {null, 0, 1, -1, Short.MAX_VALUE, Short.MIN_VALUE};
        }
        if (type == int.class) {
            return new Integer[] {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};
        }
        if (type == Integer.class) {
            return new Integer[] {null, 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};
        }
        if (type == long.class) {
            return new Long[] {0L, 1L, -1L, Long.MAX_VALUE};
        }
        if (type == Long.class) {
            return new Long[] {null, 0L, 1L, -1L, Long.MAX_VALUE};
        }
        if (type == BigInteger.class) {
            return new BigInteger[] {null, BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN,
                    BigInteger.valueOf(Long.MAX_VALUE), BigInteger.valueOf(Long.MIN_VALUE),
                    new BigInteger("123456789012345678901234567890")};
        }
        if (type == BigDecimal.class) {
            return new BigDecimal[] {null, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN,
                    new BigDecimal("123.456"), new BigDecimal("-789.012"),
                    new BigDecimal("123456789012345678901234567890.987654321")};
        }
        if (type == String.class) {
            return new String[] {null, "", "test", "®", "你好世界", "🎉🎉🎉", "123456789012345678901234567890"};
        }
        if (type == UUID.class) {
            return new UUID[] {null, UUID.randomUUID(), UUID.fromString("123e4567-e89b-12d3-a456-426614174000")};
        }
        if (type == Number.class) {
            return new Number[] {null, 0, 1, -1, Long.MAX_VALUE, Integer.MAX_VALUE};
        }
        if (type == float.class) {
            return new Float[] {0.0f, 1.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE};
        }
        if (type == Float.class) {
            return new Float[] {null, 0.0f, 1.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE};
        }
        if (type == double.class) {
            return new Double[] {0.0, 1.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE};
        }
        if (type == Double.class) {
            return new Double[] {null, 0.0, 1.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE};
        }
        if (type == boolean.class) {
            return new Boolean[] {true, false};
        }
        if (type == Boolean.class) {
            return new Boolean[] {null, true, false};
        }
        if (type == char.class) {
            return new Character[] {'a', 'z', '0', '!', '中', '国', '®'};
        }
        if (type == Character.class) {
            return new Character[] {null, 'a', 'z', '0', '!', '中', '国', '®'};
        }
        if (type == byte[].class) {
            return new Object[] {null, new byte[]{1, 2}, new byte[]{0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE}};
        }
        if (type == short[].class) {
            return new Object[] {null, new short[]{1, 2}, new short[]{0, 1, -1, Short.MAX_VALUE, Short.MIN_VALUE}};
        }
        if (type == int[].class) {
            return new Object[] {null, new int[]{1, 2}, new int[]{0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE}};
        }
        if (type == long[].class) {
            return new Object[] {null, new long[]{1, 2}, new long[]{0L, 1L, -1L, Long.MAX_VALUE}};
        }
        if (type == float[].class) {
            return new Object[] {null, new float[]{1.0f, 2.0f}, new float[]{0.0f, 1.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE}};
        }
        if (type == double[].class) {
            return new Object[] {null, new double[]{1.0, 2.0}, new double[]{0.0, 1.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE}};
        }
        if (type == boolean[].class) {
            return new Object[] {null, new boolean[]{true, false}, new boolean[]{true, false, true}};
        }
        if (type == char[].class) {
            return new Object[] {null, new char[]{'a', 'b'}, new char[]{'a', 'z', '0', '!', '\uD83D', '\uDE00'}};
        }
        if (type == String[].class) {
            return new Object[] {null, new String[0], new String[16], new String[]{"hello"}, new String[]{"", "test", "你好世界", "🎉🎉🎉", "123456789012345678901234567890"}};
        }
        if (type == java.util.Collection.class) {
            return new Object[] {null, java.util.Collections.emptyList(), java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉")};
        }
        if (type == java.util.List.class) {
            return new Object[] {null, java.util.Collections.emptyList(), java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉")};
        }
        if (type == java.util.ArrayList.class) {
            return new Object[] {null, new java.util.ArrayList<>(), new java.util.ArrayList<>(java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉"))};
        }
        if (type == java.util.LinkedList.class) {
            return new Object[] {null, new java.util.LinkedList<>(), new java.util.LinkedList<>(java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉"))};
        }
        if (type == java.util.HashSet.class) {
            return new Object[] {null, new java.util.HashSet<>(), new java.util.HashSet<>(java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉"))};
        }
        if (type == java.util.LinkedHashSet.class) {
            return new Object[] {null, new java.util.LinkedHashSet<>(), new java.util.LinkedHashSet<>(java.util.Arrays.asList("", "test", "你好世界", "🎉🎉🎉"))};
        }
        if (type == java.util.HashMap.class) {
            java.util.HashMap<String, String> map1 = new java.util.HashMap<>();
            map1.put("key1", "value1");
            java.util.HashMap<String, String> map2 = new java.util.HashMap<>();
            map2.put("", "");
            map2.put("test", "你好世界");
            map2.put("emoji", "🎉🎉🎉");
            return new Object[] {null, new java.util.HashMap<>(), map1, map2};
        }
        if (type == java.util.LinkedHashMap.class) {
            java.util.LinkedHashMap<String, String> map1 = new java.util.LinkedHashMap<>();
            map1.put("key1", "value1");
            java.util.LinkedHashMap<String, String> map2 = new java.util.LinkedHashMap<>();
            map2.put("", "");
            map2.put("test", "你好世界");
            map2.put("emoji", "🎉🎉🎉");
            return new Object[] {null, new java.util.LinkedHashMap<>(), map1, map2};
        }
        throw new JSONException("unsupported type " + type);
    }

    static ObjectReaderCreator readerCreator(boolean asm) {
        return asm ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE;
    }

    static ObjectWriterCreator writerCreator(boolean asm) {
        return asm ? ObjectWriterCreatorASM.INSTANCE : ObjectWriterCreator.INSTANCE;
    }

    @ParameterizedTest
    @MethodSource("types")
    public void types(Class<?> type) throws Exception {
        Field valueField = type.getDeclaredField("value");
        valueField.setAccessible(true);

        Object[] values = values(valueField.getType(), valueField.getGenericType());

        for (boolean asm : new boolean[] {false, true}) {
            ObjectReader<?> objectReader = readerCreator(asm).createObjectReader(type);
            ObjectWriter objectWriter = writerCreator(asm).createObjectWriter(type);

            for (Object value : values) {
                {
                    String json = JSONObject.of("value", value).toJSONString(WriteNulls);
                    byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);
                    char[] chars = json.toCharArray();
                    Object object = objectReader.readObject(of(json), 0);
                    assertEquals(value, valueField.get(object));
                    assertEquals(value, valueField.get(objectReader.readObject(of(utf8), 0)));
                    assertEquals(value, valueField.get(objectReader.readObject(of(chars), 0)));

                    try (JSONWriter jsonWriter = JSONWriter.ofUTF16(WriteNulls)) {
                        objectWriter.write(jsonWriter, object, null, null, 0);
                        assertEquals(json, objectWriter.toJSONString(object, WriteNulls));
                    }
                    try (JSONWriter jsonWriter = JSONWriter.ofUTF8(WriteNulls)) {
                        objectWriter.write(jsonWriter, object, null, null, 0);
                        assertEquals(json, objectWriter.toJSONString(object, WriteNulls));
                    }

                    try (JSONWriter jsonbWriter = JSONWriter.ofJSONB(WriteNulls)) {
                        objectWriter.writeJSONB(jsonbWriter, object, null, null, 0);
                        byte[] jsonb = jsonbWriter.getBytes();
                        assertEquals(value,
                                valueField.get(
                                        objectReader.readJSONBObject(JSONReader.ofJSONB(jsonb), null, null, 0)));
                    }
                }
                // Skip BeanToArray mode for List types as it's not compatible
                {
                    String json = JSONObject.of("value", value).toJSONString(BeanToArray);
                    byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);
                    char[] chars = json.toCharArray();
                    Object object = objectReader.readObject(of(json, JSONFactory.createReadContext(JSONReader.Feature.SupportArrayToBean)));
                    assertEquals(value, valueField.get(object));
                    assertEquals(value, valueField.get(objectReader.readObject(of(utf8, JSONFactory.createReadContext(JSONReader.Feature.SupportArrayToBean)), 0)));
                    assertEquals(value, valueField.get(objectReader.readObject(of(chars, JSONFactory.createReadContext(JSONReader.Feature.SupportArrayToBean)), 0)));

                    try (JSONWriter jsonbWriter = JSONWriter.ofJSONB(BeanToArray)) {
                        objectWriter.writeJSONB(jsonbWriter, object, null, null, 0);
                        byte[] jsonb = jsonbWriter.getBytes();
                        assertEquals(value,
                                valueField.get(
                                        objectReader.readJSONBObject(JSONReader.ofJSONB(jsonb, JSONReader.Feature.SupportArrayToBean), null, null, 0)));
                    }
                }
            }
        }
    }

    static void assertEquals(Object value, Object actualValue) {
        if (value != null && value.getClass().isArray()) {
            assertArrayEquals(value, actualValue);
        } else if (value instanceof java.util.Set && actualValue instanceof java.util.Set) {
            assertSetEquals((java.util.Set<?>) value, (java.util.Set<?>) actualValue);
        } else if (value instanceof java.util.Map && actualValue instanceof java.util.Map) {
            assertMapEquals((java.util.Map<?, ?>) value, (java.util.Map<?, ?>) actualValue);
        } else if (value instanceof java.util.Collection && actualValue instanceof java.util.Collection) {
            assertCollectionEquals((java.util.Collection<?>) value, (java.util.Collection<?>) actualValue);
        } else {
            Assertions.assertEquals(value, actualValue);
        }
    }

    static void assertCollectionEquals(java.util.Collection<?> expected, java.util.Collection<?> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        Object[] expectedArray = expected.toArray();
        Object[] actualArray = actual.toArray();
        for (int i = 0; i < expectedArray.length; i++) {
            Object exp = expectedArray[i];
            Object act = actualArray[i];
            if (exp instanceof Number && act instanceof Number) {
                Number n1 = (Number) exp;
                Number n2 = (Number) act;
                if (exp instanceof BigDecimal || act instanceof BigDecimal) {
                    Assertions.assertEquals(0, new BigDecimal(n1.toString()).compareTo(new BigDecimal(n2.toString())));
                } else if (exp instanceof BigInteger || act instanceof BigInteger) {
                    Assertions.assertEquals(0, new BigInteger(n1.toString()).compareTo(new BigInteger(n2.toString())));
                } else {
                    Assertions.assertEquals(n1.doubleValue(), n2.doubleValue(), 0.0001);
                }
            } else {
                Assertions.assertEquals(exp, act);
            }
        }
    }

    static void assertSetEquals(java.util.Set<?> expected, java.util.Set<?> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (Object exp : expected) {
            boolean found = false;
            for (Object act : actual) {
                if (Objects.equals(exp, act)) {
                    found = true;
                    break;
                }
            }
            Assertions.assertTrue(found, "Expected element not found in actual set: " + exp);
        }
    }

    static void assertMapEquals(java.util.Map<?, ?> expected, java.util.Map<?, ?> actual) {
        Assertions.assertEquals(expected.size(), actual.size());
        for (java.util.Map.Entry<?, ?> entry : expected.entrySet()) {
            Object key = entry.getKey();
            Object expValue = entry.getValue();
            Object actValue = actual.get(key);
            if (expValue != null && expValue.getClass().isArray()) {
                assertArrayEquals(expValue, actValue);
            } else {
                Assertions.assertEquals(expValue, actValue, "Value mismatch for key: " + key);
            }
        }
    }

    static void assertArrayEquals(Object expected, Object actual) {
        if (expected == null) {
            assertEquals(expected, actual);
            return;
        }

        if (expected instanceof byte[] && actual instanceof byte[]) {
            assertEquals(java.util.Arrays.equals((byte[]) expected, (byte[]) actual), true);
        } else if (expected instanceof short[] && actual instanceof short[]) {
            assertEquals(java.util.Arrays.equals((short[]) expected, (short[]) actual), true);
        } else if (expected instanceof int[] && actual instanceof int[]) {
            assertEquals(java.util.Arrays.equals((int[]) expected, (int[]) actual), true);
        } else if (expected instanceof long[] && actual instanceof long[]) {
            assertEquals(java.util.Arrays.equals((long[]) expected, (long[]) actual), true);
        } else if (expected instanceof float[] && actual instanceof float[]) {
            assertEquals(java.util.Arrays.equals((float[]) expected, (float[]) actual), true);
        } else if (expected instanceof double[] && actual instanceof double[]) {
            assertEquals(java.util.Arrays.equals((double[]) expected, (double[]) actual), true);
        } else if (expected instanceof boolean[] && actual instanceof boolean[]) {
            assertEquals(java.util.Arrays.equals((boolean[]) expected, (boolean[]) actual), true);
        } else if (expected instanceof char[] && actual instanceof char[]) {
            assertEquals(java.util.Arrays.equals((char[]) expected, (char[]) actual), true);
        } else if (expected instanceof String[] && actual instanceof String[]) {
            assertEquals(java.util.Arrays.equals((String[]) expected, (String[]) actual), true);
        } else {
            assertEquals(expected, actual);
        }
    }

    // byte/Byte test classes
    public static class ByteF {
        public Byte value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class ByteF1 {
        public Byte value;
    }

    public static class ByteF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Byte value;
    }

    public static class ByteM {
        private Byte value;
        public Byte getValue() {
            return value;
        }
        public void setValue(Byte value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class ByteM1 {
        private Byte value;
        public Byte getValue() {
            return value;
        }
        public void setValue(Byte value) {
            this.value = value;
        }
    }

    public static class ByteM2 {
        private Byte value;
        @JSONField(serializeFeatures = WriteNulls)
        public Byte getValue() {
            return value;
        }
        public void setValue(Byte value) {
            this.value = value;
        }
    }

    public static class ByteC {
        private final Byte value;
        public ByteC(Byte value) {
            this.value = value;
        }
        public Byte getValue() {
            return value;
        }
    }

    public static class ByteValueF {
        public byte value;
    }

    public static class ByteValueM {
        private byte value;
        public byte getValue() {
            return value;
        }
        public void setValue(byte value) {
            this.value = value;
        }
    }

    public static class ByteValueC {
        private final byte value;
        public ByteValueC(byte value) {
            this.value = value;
        }
        public byte getValue() {
            return value;
        }
    }

    // short/Short test classes
    public static class ShortF {
        public Short value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class ShortF1 {
        public Short value;
    }

    public static class ShortF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Short value;
    }

    public static class ShortM {
        private Short value;
        public Short getValue() {
            return value;
        }
        public void setValue(Short value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class ShortM1 {
        private Short value;
        public Short getValue() {
            return value;
        }
        public void setValue(Short value) {
            this.value = value;
        }
    }

    public static class ShortM2 {
        private Short value;
        @JSONField(serializeFeatures = WriteNulls)
        public Short getValue() {
            return value;
        }
        public void setValue(Short value) {
            this.value = value;
        }
    }

    public static class ShortC {
        private final Short value;
        public ShortC(Short value) {
            this.value = value;
        }
        public Short getValue() {
            return value;
        }
    }

    public static class ShortValueF {
        public short value;
    }

    public static class ShortValueM {
        private short value;
        public short getValue() {
            return value;
        }
        public void setValue(short value) {
            this.value = value;
        }
    }

    public static class ShortValueC {
        private final short value;
        public ShortValueC(short value) {
            this.value = value;
        }
        public short getValue() {
            return value;
        }
    }

    // int/Integer test classes
    public static class IntF {
        public Integer value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class IntegerF1 {
        public Integer value;
    }

    public static class IntegerF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Integer value;
    }

    public static class IntM {
        private Integer value;
        public Integer getValue() {
            return value;
        }
        public void setValue(Integer value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class IntegerM1 {
        private Integer value;
        public Integer getValue() {
            return value;
        }
        public void setValue(Integer value) {
            this.value = value;
        }
    }

    public static class IntegerM2 {
        private Integer value;
        @JSONField(serializeFeatures = WriteNulls)
        public Integer getValue() {
            return value;
        }
        public void setValue(Integer value) {
            this.value = value;
        }
    }

    public static class IntC {
        private final Integer value;
        public IntC(Integer value) {
            this.value = value;
        }
        public Integer getValue() {
            return value;
        }
    }

    public static class IntValueF {
        public int value;
    }

    public static class IntValueM {
        private int value;
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class IntValueC {
        private final int value;
        public IntValueC(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    // long/Long test classes
    public static class LongF {
        public Long value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class LongF1 {
        public Long value;
    }

    public static class LongF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Long value;
    }

    public static class LongM {
        private Long value;
        public Long getValue() {
            return value;
        }
        public void setValue(Long value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class LongM1 {
        private Long value;
        public Long getValue() {
            return value;
        }
        public void setValue(Long value) {
            this.value = value;
        }
    }

    public static class LongM2 {
        private Long value;
        @JSONField(serializeFeatures = WriteNulls)
        public Long getValue() {
            return value;
        }
        public void setValue(Long value) {
            this.value = value;
        }
    }

    public static class LongC {
        private final Long value;
        public LongC(Long value) {
            this.value = value;
        }
        public Long getValue() {
            return value;
        }
    }

    public static class LongValueF {
        public long value;
    }

    public static class LongValueM {
        private long value;
        public long getValue() {
            return value;
        }
        public void setValue(long value) {
            this.value = value;
        }
    }

    public static class LongValueC {
        private final long value;
        public LongValueC(long value) {
            this.value = value;
        }
        public long getValue() {
            return value;
        }
    }

    // BigInteger test classes
    public static class BigIntegerF {
        public BigInteger value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BigIntegerF1 {
        public BigInteger value;
    }

    public static class BigIntegerF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public BigInteger value;
    }

    public static class BigIntegerM {
        private BigInteger value;
        public BigInteger getValue() {
            return value;
        }
        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BigIntegerM1 {
        private BigInteger value;
        public BigInteger getValue() {
            return value;
        }
        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    public static class BigIntegerM2 {
        private BigInteger value;
        @JSONField(serializeFeatures = WriteNulls)
        public BigInteger getValue() {
            return value;
        }
        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    public static class BigIntegerC {
        private final BigInteger value;
        public BigIntegerC(BigInteger value) {
            this.value = value;
        }
        public BigInteger getValue() {
            return value;
        }
    }

    // BigDecimal test classes
    public static class BigDecimalF {
        public BigDecimal value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BigDecimalF1 {
        public BigDecimal value;
    }

    public static class BigDecimalF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public BigDecimal value;
    }

    public static class BigDecimalM {
        private BigDecimal value;
        public BigDecimal getValue() {
            return value;
        }
        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BigDecimalM1 {
        private BigDecimal value;
        public BigDecimal getValue() {
            return value;
        }
        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class BigDecimalM2 {
        private BigDecimal value;
        @JSONField(serializeFeatures = WriteNulls)
        public BigDecimal getValue() {
            return value;
        }
        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class BigDecimalC {
        private final BigDecimal value;
        public BigDecimalC(BigDecimal value) {
            this.value = value;
        }
        public BigDecimal getValue() {
            return value;
        }
    }

    // String test classes
    public static class StringF {
        public String value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class StringF1 {
        public String value;
    }

    public static class StringF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public String value;
    }

    public static class StringM {
        private String value;
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class StringM1 {
        private String value;
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class StringM2 {
        private String value;
        @JSONField(serializeFeatures = WriteNulls)
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class StringC {
        private final String value;
        public StringC(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    // UUID test classes
    public static class UUIDF {
        public UUID value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class UUIDF1 {
        public UUID value;
    }

    public static class UUIDF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public UUID value;
    }

    public static class UUIDM {
        private UUID value;
        public UUID getValue() {
            return value;
        }
        public void setValue(UUID value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class UUIDM1 {
        private UUID value;
        public UUID getValue() {
            return value;
        }
        public void setValue(UUID value) {
            this.value = value;
        }
    }

    public static class UUIDM2 {
        private UUID value;
        @JSONField(serializeFeatures = WriteNulls)
        public UUID getValue() {
            return value;
        }
        public void setValue(UUID value) {
            this.value = value;
        }
    }

    public static class UUIDC {
        private final UUID value;
        public UUIDC(UUID value) {
            this.value = value;
        }
        public UUID getValue() {
            return value;
        }
    }

    // Number test classes
    public static class NumberF {
        public Number value;
    }

    public static class NumberM {
        private Number value;
        public Number getValue() {
            return value;
        }
        public void setValue(Number value) {
            this.value = value;
        }
    }

    public static class NumberC {
        private final Number value;
        public NumberC(Number value) {
            this.value = value;
        }
        public Number getValue() {
            return value;
        }
    }

    // float/Float test classes
    public static class FloatF {
        public Float value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class FloatF1 {
        public Float value;
    }

    public static class FloatF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Float value;
    }

    public static class FloatM {
        private Float value;
        public Float getValue() {
            return value;
        }
        public void setValue(Float value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class FloatM1 {
        private Float value;
        public Float getValue() {
            return value;
        }
        public void setValue(Float value) {
            this.value = value;
        }
    }

    public static class FloatM2 {
        private Float value;
        @JSONField(serializeFeatures = WriteNulls)
        public Float getValue() {
            return value;
        }
        public void setValue(Float value) {
            this.value = value;
        }
    }

    public static class FloatC {
        private final Float value;
        public FloatC(Float value) {
            this.value = value;
        }
        public Float getValue() {
            return value;
        }
    }

    public static class FloatValueF {
        public float value;
    }

    public static class FloatValueM {
        private float value;
        public float getValue() {
            return value;
        }
        public void setValue(float value) {
            this.value = value;
        }
    }

    public static class FloatValueC {
        private final float value;
        public FloatValueC(float value) {
            this.value = value;
        }
        public float getValue() {
            return value;
        }
    }

    // double/Double test classes
    public static class DoubleF {
        public Double value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class DoubleF1 {
        public Double value;
    }

    public static class DoubleF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Double value;
    }

    public static class DoubleM {
        private Double value;
        public Double getValue() {
            return value;
        }
        public void setValue(Double value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class DoubleM1 {
        private Double value;
        public Double getValue() {
            return value;
        }
        public void setValue(Double value) {
            this.value = value;
        }
    }

    public static class DoubleM2 {
        private Double value;
        @JSONField(serializeFeatures = WriteNulls)
        public Double getValue() {
            return value;
        }
        public void setValue(Double value) {
            this.value = value;
        }
    }

    public static class DoubleC {
        private final Double value;
        public DoubleC(Double value) {
            this.value = value;
        }
        public Double getValue() {
            return value;
        }
    }

    public static class DoubleValueF {
        public double value;
    }

    public static class DoubleValueM {
        private double value;
        public double getValue() {
            return value;
        }
        public void setValue(double value) {
            this.value = value;
        }
    }

    public static class DoubleValueC {
        private final double value;
        public DoubleValueC(double value) {
            this.value = value;
        }
        public double getValue() {
            return value;
        }
    }

    // boolean/Boolean test classes
    public static class BooleanF {
        public Boolean value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BooleanF1 {
        public Boolean value;
    }

    public static class BooleanF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Boolean value;
    }

    public static class BooleanM {
        private Boolean value;
        public Boolean getValue() {
            return value;
        }
        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class BooleanM1 {
        private Boolean value;
        public Boolean getValue() {
            return value;
        }
        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    public static class BooleanM2 {
        private Boolean value;
        @JSONField(serializeFeatures = WriteNulls)
        public Boolean getValue() {
            return value;
        }
        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    public static class BooleanC {
        private final Boolean value;
        public BooleanC(Boolean value) {
            this.value = value;
        }
        public Boolean getValue() {
            return value;
        }
    }

    public static class BooleanValueF {
        public boolean value;
    }

    public static class BooleanValueM {
        private boolean value;
        public boolean getValue() {
            return value;
        }
        public void setValue(boolean value) {
            this.value = value;
        }
    }

    public static class BooleanValueC {
        private final boolean value;
        public BooleanValueC(boolean value) {
            this.value = value;
        }
        public boolean getValue() {
            return value;
        }
    }

    // char/Character test classes
    public static class CharacterF {
        public Character value;
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class CharacterF1 {
        public Character value;
    }

    public static class CharacterF2 {
        @JSONField(serializeFeatures = WriteNulls)
        public Character value;
    }

    public static class CharacterM {
        private Character value;
        public Character getValue() {
            return value;
        }
        public void setValue(Character value) {
            this.value = value;
        }
    }

    @JSONType(serializeFeatures = WriteNulls)
    public static class CharacterM1 {
        private Character value;
        public Character getValue() {
            return value;
        }
        public void setValue(Character value) {
            this.value = value;
        }
    }

    public static class CharacterM2 {
        private Character value;
        @JSONField(serializeFeatures = WriteNulls)
        public Character getValue() {
            return value;
        }
        public void setValue(Character value) {
            this.value = value;
        }
    }

    public static class CharacterC {
        private final Character value;
        public CharacterC(Character value) {
            this.value = value;
        }
        public Character getValue() {
            return value;
        }
    }

    public static class CharValueF {
        public char value;
    }

    public static class CharValueM {
        private char value;
        public char getValue() {
            return value;
        }
        public void setValue(char value) {
            this.value = value;
        }
    }

    public static class CharValueC {
        private final char value;
        public CharValueC(char value) {
            this.value = value;
        }
        public char getValue() {
            return value;
        }
    }

    // Array test classes (only field-based since arrays are mutable objects)
    public static class ByteArrayF {
        public byte[] value;
    }

    public static class ShortArrayF {
        public short[] value;
    }

    public static class IntArrayF {
        public int[] value;
    }

    public static class LongArrayF {
        public long[] value;
    }

    public static class FloatArrayF {
        public float[] value;
    }

    public static class DoubleArrayF {
        public double[] value;
    }

    public static class BooleanArrayF {
        public boolean[] value;
    }

    public static class CharArrayF {
        public char[] value;
    }

    public static class StringArrayF {
        public String[] value;
    }

    public static class ByteArrayM {
        private byte[] value;
        public byte[] getValue() {
            return value;
        }
        public void setValue(byte[] value) {
            this.value = value;
        }
    }

    public static class ShortArrayM {
        private short[] value;
        public short[] getValue() {
            return value;
        }
        public void setValue(short[] value) {
            this.value = value;
        }
    }

    public static class IntArrayM {
        private int[] value;
        public int[] getValue() {
            return value;
        }
        public void setValue(int[] value) {
            this.value = value;
        }
    }

    public static class LongArrayM {
        private long[] value;
        public long[] getValue() {
            return value;
        }
        public void setValue(long[] value) {
            this.value = value;
        }
    }

    public static class FloatArrayM {
        private float[] value;
        public float[] getValue() {
            return value;
        }
        public void setValue(float[] value) {
            this.value = value;
        }
    }

    public static class DoubleArrayM {
        private double[] value;
        public double[] getValue() {
            return value;
        }
        public void setValue(double[] value) {
            this.value = value;
        }
    }

    public static class BooleanArrayM {
        private boolean[] value;
        public boolean[] getValue() {
            return value;
        }
        public void setValue(boolean[] value) {
            this.value = value;
        }
    }

    public static class CharArrayM {
        private char[] value;
        public char[] getValue() {
            return value;
        }
        public void setValue(char[] value) {
            this.value = value;
        }
    }

    public static class StringArrayM {
        private String[] value;
        public String[] getValue() {
            return value;
        }
        public void setValue(String[] value) {
            this.value = value;
        }
    }

    public static class ByteArrayC {
        private final byte[] value;
        public ByteArrayC(byte[] value) {
            this.value = value;
        }
        public byte[] getValue() {
            return value;
        }
    }

    public static class ShortArrayC {
        private final short[] value;
        public ShortArrayC(short[] value) {
            this.value = value;
        }
        public short[] getValue() {
            return value;
        }
    }

    public static class IntArrayC {
        private final int[] value;
        public IntArrayC(int[] value) {
            this.value = value;
        }
        public int[] getValue() {
            return value;
        }
    }

    public static class LongArrayC {
        private final long[] value;
        public LongArrayC(long[] value) {
            this.value = value;
        }
        public long[] getValue() {
            return value;
        }
    }

    public static class FloatArrayC {
        private final float[] value;
        public FloatArrayC(float[] value) {
            this.value = value;
        }
        public float[] getValue() {
            return value;
        }
    }

    public static class DoubleArrayC {
        private final double[] value;
        public DoubleArrayC(double[] value) {
            this.value = value;
        }
        public double[] getValue() {
            return value;
        }
    }

    public static class BooleanArrayC {
        private final boolean[] value;
        public BooleanArrayC(boolean[] value) {
            this.value = value;
        }
        public boolean[] getValue() {
            return value;
        }
    }

    public static class CharArrayC {
        private final char[] value;
        public CharArrayC(char[] value) {
            this.value = value;
        }
        public char[] getValue() {
            return value;
        }
    }

    public static class StringArrayC {
        private final String[] value;
        public StringArrayC(String[] value) {
            this.value = value;
        }
        public String[] getValue() {
            return value;
        }
    }

    // Collection/List test classes
    public static class CollectionStringF {
        public java.util.Collection<String> value;
    }

    public static class CollectionStringM {
        private java.util.Collection<String> value;
        public java.util.Collection<String> getValue() {
            return value;
        }
        public void setValue(java.util.Collection<String> value) {
            this.value = value;
        }
    }

    public static class ListStringF {
        public java.util.List<String> value;
    }

    public static class ListStringM {
        private java.util.List<String> value;
        public java.util.List<String> getValue() {
            return value;
        }
        public void setValue(java.util.List<String> value) {
            this.value = value;
        }
    }

    // Collection constructor-based test class
    public static class CollectionStringC {
        private final java.util.Collection<String> value;
        public CollectionStringC(java.util.Collection<String> value) {
            this.value = value;
        }
        public java.util.Collection<String> getValue() {
            return value;
        }
    }

    // List constructor-based test class
    public static class ListStringC {
        private final java.util.List<String> value;
        public ListStringC(java.util.List<String> value) {
            this.value = value;
        }
        public java.util.List<String> getValue() {
            return value;
        }
    }

    // ArrayList test classes
    public static class ArrayListStringF {
        public java.util.ArrayList<String> value;
    }

    public static class ArrayListStringM {
        private java.util.ArrayList<String> value;
        public java.util.ArrayList<String> getValue() {
            return value;
        }
        public void setValue(java.util.ArrayList<String> value) {
            this.value = value;
        }
    }

    // LinkedList test classes
    public static class LinkedListStringF {
        public java.util.LinkedList<String> value;
    }

    public static class LinkedListStringM {
        private java.util.LinkedList<String> value;
        public java.util.LinkedList<String> getValue() {
            return value;
        }
        public void setValue(java.util.LinkedList<String> value) {
            this.value = value;
        }
    }

    // ArrayList constructor-based test class
    public static class ArrayListStringC {
        private final java.util.ArrayList<String> value;
        public ArrayListStringC(java.util.ArrayList<String> value) {
            this.value = value;
        }
        public java.util.ArrayList<String> getValue() {
            return value;
        }
    }

    // LinkedList constructor-based test class
    public static class LinkedListStringC {
        private final java.util.LinkedList<String> value;
        public LinkedListStringC(java.util.LinkedList<String> value) {
            this.value = value;
        }
        public java.util.LinkedList<String> getValue() {
            return value;
        }
    }

    // List<Byte> test classes
    public static class ListByteF {
        public java.util.List<Byte> value;
    }

    public static class ListByteM {
        private java.util.List<Byte> value;
        public java.util.List<Byte> getValue() {
            return value;
        }
        public void setValue(java.util.List<Byte> value) {
            this.value = value;
        }
    }

    public static class ListByteC {
        private final java.util.List<Byte> value;
        public ListByteC(java.util.List<Byte> value) {
            this.value = value;
        }
        public java.util.List<Byte> getValue() {
            return value;
        }
    }

    // List<Short> test classes
    public static class ListShortF {
        public java.util.List<Short> value;
    }

    public static class ListShortM {
        private java.util.List<Short> value;
        public java.util.List<Short> getValue() {
            return value;
        }
        public void setValue(java.util.List<Short> value) {
            this.value = value;
        }
    }

    public static class ListShortC {
        private final java.util.List<Short> value;
        public ListShortC(java.util.List<Short> value) {
            this.value = value;
        }
        public java.util.List<Short> getValue() {
            return value;
        }
    }

    // List<Integer> test classes
    public static class ListIntegerF {
        public java.util.List<Integer> value;
    }

    public static class ListIntegerM {
        private java.util.List<Integer> value;
        public java.util.List<Integer> getValue() {
            return value;
        }
        public void setValue(java.util.List<Integer> value) {
            this.value = value;
        }
    }

    public static class ListIntegerC {
        private final java.util.List<Integer> value;
        public ListIntegerC(java.util.List<Integer> value) {
            this.value = value;
        }
        public java.util.List<Integer> getValue() {
            return value;
        }
    }

    // List<Long> test classes
    public static class ListLongF {
        public java.util.List<Long> value;
    }

    public static class ListLongM {
        private java.util.List<Long> value;
        public java.util.List<Long> getValue() {
            return value;
        }
        public void setValue(java.util.List<Long> value) {
            this.value = value;
        }
    }

    public static class ListLongC {
        private final java.util.List<Long> value;
        public ListLongC(java.util.List<Long> value) {
            this.value = value;
        }
        public java.util.List<Long> getValue() {
            return value;
        }
    }

    // List<BigInteger> test classes
    public static class ListBigIntegerF {
        public java.util.List<BigInteger> value;
    }

    public static class ListBigIntegerM {
        private java.util.List<BigInteger> value;
        public java.util.List<BigInteger> getValue() {
            return value;
        }
        public void setValue(java.util.List<BigInteger> value) {
            this.value = value;
        }
    }

    public static class ListBigIntegerC {
        private final java.util.List<BigInteger> value;
        public ListBigIntegerC(java.util.List<BigInteger> value) {
            this.value = value;
        }
        public java.util.List<BigInteger> getValue() {
            return value;
        }
    }

    // List<BigDecimal> test classes
    public static class ListBigDecimalF {
        public java.util.List<BigDecimal> value;
    }

    public static class ListBigDecimalM {
        private java.util.List<BigDecimal> value;
        public java.util.List<BigDecimal> getValue() {
            return value;
        }
        public void setValue(java.util.List<BigDecimal> value) {
            this.value = value;
        }
    }

    public static class ListBigDecimalC {
        private final java.util.List<BigDecimal> value;
        public ListBigDecimalC(java.util.List<BigDecimal> value) {
            this.value = value;
        }
        public java.util.List<BigDecimal> getValue() {
            return value;
        }
    }

    // List<Number> test classes
    public static class ListNumberF {
        public java.util.List<Number> value;
    }

    public static class ListNumberM {
        private java.util.List<Number> value;
        public java.util.List<Number> getValue() {
            return value;
        }
        public void setValue(java.util.List<Number> value) {
            this.value = value;
        }
    }

    public static class ListNumberC {
        private final java.util.List<Number> value;
        public ListNumberC(java.util.List<Number> value) {
            this.value = value;
        }
        public java.util.List<Number> getValue() {
            return value;
        }
    }

    // HashSet test classes
    public static class HashSetStringF {
        public java.util.HashSet<String> value;
    }

    public static class HashSetStringM {
        private java.util.HashSet<String> value;
        public java.util.HashSet<String> getValue() {
            return value;
        }
        public void setValue(java.util.HashSet<String> value) {
            this.value = value;
        }
    }

    public static class HashSetStringC {
        private final java.util.HashSet<String> value;
        public HashSetStringC(java.util.HashSet<String> value) {
            this.value = value;
        }
        public java.util.HashSet<String> getValue() {
            return value;
        }
    }

    // LinkedHashSet test classes
    public static class LinkedHashSetStringF {
        public java.util.LinkedHashSet<String> value;
    }

    public static class LinkedHashSetStringM {
        private java.util.LinkedHashSet<String> value;
        public java.util.LinkedHashSet<String> getValue() {
            return value;
        }
        public void setValue(java.util.LinkedHashSet<String> value) {
            this.value = value;
        }
    }

    public static class LinkedHashSetStringC {
        private final java.util.LinkedHashSet<String> value;
        public LinkedHashSetStringC(java.util.LinkedHashSet<String> value) {
            this.value = value;
        }
        public java.util.LinkedHashSet<String> getValue() {
            return value;
        }
    }

    // HashMap test classes
    public static class HashMapStringF {
        public java.util.HashMap<String, String> value;
    }

    public static class HashMapStringM {
        private java.util.HashMap<String, String> value;
        public java.util.HashMap<String, String> getValue() {
            return value;
        }
        public void setValue(java.util.HashMap<String, String> value) {
            this.value = value;
        }
    }

    public static class HashMapStringC {
        private final java.util.HashMap<String, String> value;
        public HashMapStringC(java.util.HashMap<String, String> value) {
            this.value = value;
        }
        public java.util.HashMap<String, String> getValue() {
            return value;
        }
    }

    // LinkedHashMap test classes
    public static class LinkedHashMapStringF {
        public java.util.LinkedHashMap<String, String> value;
    }

    public static class LinkedHashMapStringM {
        private java.util.LinkedHashMap<String, String> value;
        public java.util.LinkedHashMap<String, String> getValue() {
            return value;
        }
        public void setValue(java.util.LinkedHashMap<String, String> value) {
            this.value = value;
        }
    }

    public static class LinkedHashMapStringC {
        private final java.util.LinkedHashMap<String, String> value;
        public LinkedHashMapStringC(java.util.LinkedHashMap<String, String> value) {
            this.value = value;
        }
        public java.util.LinkedHashMap<String, String> getValue() {
            return value;
        }
    }
}
