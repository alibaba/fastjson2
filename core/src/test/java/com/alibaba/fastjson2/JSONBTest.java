package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class JSONBTest {
    public static final int INT24_MAX = 256 * 256 * 128 - 1;
    public static final int INT24_MIN = -(256 * 256 * 128);

    @Test
    public void test_object_empty() {
        byte[] jsonbBytes = JSONB.toBytes(new HashMap());
        Map object = (Map) JSONB.parseObject(jsonbBytes, Map.class);
        assertTrue(object.isEmpty());
    }

    @Test
    public void test_object_one() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 123));
        Map object = (Map) JSONB.parseObject(jsonbBytes, Map.class);
        assertEquals(1, object.size());
        assertEquals(123, ((Number) object.get("id")).intValue());
    }

    @Test
    public void test_parse_object_typed_empty() {
        Type[] types = new Type[]{
                Object.class,
                Cloneable.class,
                Closeable.class,
                Serializable.class
        };

        byte[] jsonbBytes = JSONB.toBytes(new HashMap());
        for (Type type : types) {
            Map object = JSONB.parseObject(jsonbBytes, type);
            assertNotNull(object, type.getTypeName());
            assertTrue(object.isEmpty());
        }
    }

    @Test
    public void test_parse_object_typed_map_str_str() {
        Type[] types = new Type[]{
                Map.class,
                AbstractMap.class,
                ConcurrentMap.class,
                ConcurrentHashMap.class,
                ConcurrentNavigableMap.class,
                ConcurrentSkipListMap.class,
                LinkedHashMap.class,
                HashMap.class,
                TreeMap.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, type);

            Map object = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, object.size());
            assertEquals("123", object.get("id"));
        }
    }

    @Test
    public void test_parse_object_typed_map_str_long() {
        Type[] types = new Type[]{
                Map.class,
                AbstractMap.class,
                ConcurrentMap.class,
                ConcurrentHashMap.class,
                ConcurrentNavigableMap.class,
                ConcurrentSkipListMap.class,
                LinkedHashMap.class,
                HashMap.class,
                TreeMap.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singletonMap("id", 123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class, Long.class}, null, type);

            Map object = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, object.size());
            assertEquals(123L, object.get("id"));
        }
    }

    @Test
    public void test_parse_object_typed_list() {
        Type[] types = new Type[]{
                Object.class,
                Iterable.class,
                Collection.class,
                List.class,
                AbstractCollection.class,
                AbstractList.class,
                ArrayList.class,
                CopyOnWriteArrayList.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));
        for (Type type : types) {
            List list = (List) JSONB.parseObject(jsonbBytes, type);
            assertEquals(1, list.size());
            assertEquals(123, ((Number) list.get(0)).intValue());
        }
    }

    @Test
    public void test_parse_object_typed_list_parm_emtpy() {
        Type[] types = new Type[]{
                Object.class,
                Iterable.class,
                Collection.class,
                List.class,
                AbstractCollection.class,
                AbstractList.class,
                ArrayList.class,
                CopyOnWriteArrayList.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{}, null, type);
            List<Number> list = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get().intValue());
        }
    }

    @Test
    public void test_parse_object_typed_list_str() {
        Type[] types = new Type[]{
                Iterable.class,
                Collection.class,
                List.class,
                AbstractCollection.class,
                AbstractList.class,
                ArrayList.class,
                CopyOnWriteArrayList.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            List<String> list = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals("123", list.stream().findFirst().get());
        }
    }

    @Test
    public void test_parse_object_typed_list_long() {
        Type[] types = new Type[]{
                Iterable.class,
                Collection.class,
                List.class,
                AbstractCollection.class,
                AbstractList.class,
                ArrayList.class,
                CopyOnWriteArrayList.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            List list = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals(123L, list.stream().findFirst().get());
        }
    }

    @Test
    public void test_parse_object_typed_queue() {
        Type[] types = new Type[]{
                Queue.class,
                Deque.class,
                AbstractSequentialList.class,
                LinkedList.class,
                ConcurrentLinkedDeque.class,
                ConcurrentLinkedQueue.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            Collection<Number> list = (Collection) JSONB.parseObject(jsonbBytes, type);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get().intValue());
        }
    }

    @Test
    public void test_parse_object_typed_queue_str() {
        Type[] types = new Type[]{
                Queue.class,
                Deque.class,
                AbstractSequentialList.class,
                LinkedList.class,
                ConcurrentLinkedDeque.class,
                ConcurrentLinkedQueue.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            Collection list = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals("123", list.stream().findFirst().get());
        }
    }

    @Test
    public void test_parse_object_typed_queue_long() {
        Type[] types = new Type[]{
                Queue.class,
                Deque.class,
                AbstractSequentialList.class,
                LinkedList.class,
                ConcurrentLinkedDeque.class,
                ConcurrentLinkedQueue.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            Collection list = JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals(123L, list.stream().findFirst().get());
        }
    }

    @Test
    public void test_parse_object_typed_set() {
        Type[] types = new Type[]{
                Set.class,
                NavigableSet.class,
                SortedSet.class,
                AbstractSet.class,
                TreeSet.class,
                HashSet.class,
                LinkedHashSet.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            Set<Number> list = (Set) JSONB.parseObject(jsonbBytes, type);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get().intValue());
        }
    }

    @Test
    public void test_parse_object_typed_set_str() {
        Type[] types = new Type[]{
                Set.class,
                NavigableSet.class,
                SortedSet.class,
                AbstractSet.class,
                TreeSet.class,
                HashSet.class,
                LinkedHashSet.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            Set list = (Set) JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            assertEquals("123", list.stream().findFirst().get());
        }
    }

    @Test
    public void test_parse_object_typed_set_long() {
        Type[] types = new Type[]{
                Set.class,
                NavigableSet.class,
                SortedSet.class,
                AbstractSet.class,
                TreeSet.class,
                HashSet.class,
                LinkedHashSet.class,
        };

        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            Set list = (Set) JSONB.parseObject(jsonbBytes, parameterizedType);
            assertEquals(1, list.size());
            Optional first = list.stream().findFirst();
            assertEquals(123L, first.get());
        }
    }

    @Test
    public void test_array_empty() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.emptyList());

        List list = (List) JSONB.parseObject(jsonbBytes, Object.class);
        assertTrue(list.isEmpty());
    }

    @Test
    public void test_array_one() {
        byte[] jsonbBytes = JSONB.toBytes(Collections.singleton(123));

        List<Number> list = (List) JSONB.parseObject(jsonbBytes, Object.class);
        assertEquals(1, list.size());
        assertEquals(123, list.get(0).intValue());
    }

    @Test
    public void test_null() {
        byte[] jsonbBytes = JSONB.toBytes((Collection) null);
        assertNull(JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_null_features() {
        byte[] jsonbBytes = JSONB.toBytes((Object) null, JSONWriter.Feature.WriteNulls);
        assertNull(JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_null_features1() {
        byte[] jsonbBytes = JSONB.toBytes((Object) null, new JSONWriter.Context(JSONFactory.getDefaultObjectWriterProvider(), JSONWriter.Feature.WriteNulls));
        assertNull(JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_true() {
        byte[] jsonbBytes = JSONB.toBytes(true);
        assertTrue((Boolean) JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_false() {
        byte[] jsonbBytes = JSONB.toBytes(false);
        assertFalse((Boolean) JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_str() {
        String str = "wenshao";
        byte[] jsonbBytes = JSONB.toBytes(str);
        assertEquals(str, JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_str_ascii() {
        String str = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        byte[] jsonbBytes = JSONB.toBytes(str);
        assertEquals(str, JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_str_ascii_1() {
        String str = "com.alibaba.trade.buy.new_ultronage.presentation.param.RichTextVO$EventVO";
        byte[] jsonbBytes = JSONB.toBytes(str);
        byte[] jsonbBytes2 = JSONB.toBytes((Object) str);
        assertArrayEquals(jsonbBytes2, jsonbBytes);
        assertEquals(str, JSONB.parseObject(jsonbBytes, String.class));
    }

    @Test
    public void test_str_utf8() {
        String str = "速度最快";
        byte[] jsonbBytes = JSONB.toBytes(str);
        assertEquals(str, JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_str_utf16() {
        String str = "速度最快";
        byte[] jsonbBytes = JSONB.toBytes(str, StandardCharsets.UTF_16);
        assertEquals(str, JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_str_utf16_big() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            buf.append("速度最快");
        }
        String str = buf.toString();
        byte[] jsonbBytes = JSONB.toBytes((Object) str);
        assertEquals(str, JSONB.parseObject(jsonbBytes, Object.class));
    }

    @Test
    public void test_num_1() {
        byte[] jsonbBytes = JSONB.toBytes(0);
        assertEquals(0, ((Number) JSONB.parseObject(jsonbBytes, Object.class)).intValue());
    }

    @Test
    public void test_num_int() {
        int[] numbers = new int[]{
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                1000000000
        };
        for (int i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(i, ((Number) JSONB.parseObject(jsonbBytes, Object.class)).intValue());
        }
    }

    @Test
    public void test_num_int_cast_str() {
        int[] numbers = new int[]{
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                1000000000
        };
        for (int i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(Integer.toString(i), JSONB.parseObject(jsonbBytes, String.class));
        }
    }

    @Test
    public void test_num_str_cast_int() {
        int[] numbers = new int[]{
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                1000000000
        };
        for (int i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(Integer.toString(i));
            assertEquals((Integer) i, JSONB.parseObject(jsonbBytes, Integer.class));
        }
    }

    @Test
    public void test_num_long() {
        long[] numbers = new long[]{
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L
        };
        for (long i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(i, ((Number) JSONB.parseObject(jsonbBytes, Object.class)).longValue());
        }
    }

    @Test
    public void test_num_long_1() {
        long[] numbers = new long[]{
                2000
        };
        for (long i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(i, ((Number) JSONB.parseObject(jsonbBytes, Object.class)).longValue());
        }
    }

    @Test
    public void test_num_long_cast_str() {
        long[] numbers = new long[]{
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L
        };
        for (long i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(Long.toString(i), JSONB.parseObject(jsonbBytes, String.class));
        }
    }

    @Test
    public void test_num_str_cast_long() {
        long[] numbers = new long[]{
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L
        };
        for (long i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(Long.toString(i));
            assertEquals(i, ((Long) JSONB.parseObject(jsonbBytes, Long.class)).longValue());
        }
    }

    @Test
    public void test_bigint_cast_str() {
        BigInteger[] numbers = new BigInteger[]{
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.TEN,
                BigInteger.valueOf(Long.MIN_VALUE),
                BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("12345678901234567890123456789012345678901234567890123456789012345678901234567890"),
        };
        for (BigInteger i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(i.toString(), JSONB.parseObject(jsonbBytes, String.class));
        }
    }

    @Test
    public void test_bigdecimal_cast_str() {
        BigDecimal[] numbers = new BigDecimal[]{
                BigDecimal.ZERO,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.valueOf(Byte.MIN_VALUE),
                BigDecimal.valueOf(Byte.MAX_VALUE),
                BigDecimal.valueOf(INT24_MIN),
                BigDecimal.valueOf(INT24_MAX),
                BigDecimal.valueOf(Short.MIN_VALUE),
                BigDecimal.valueOf(Short.MAX_VALUE),
                BigDecimal.valueOf(Integer.MIN_VALUE),
                BigDecimal.valueOf(Integer.MAX_VALUE),
                BigDecimal.valueOf(Long.MIN_VALUE),
                BigDecimal.valueOf(Long.MAX_VALUE),
                BigDecimal.valueOf(Byte.MIN_VALUE, 1),
                BigDecimal.valueOf(Byte.MAX_VALUE, 2),
                BigDecimal.valueOf(INT24_MIN, 1),
                BigDecimal.valueOf(INT24_MAX, 2),
                BigDecimal.valueOf(Short.MIN_VALUE, 1),
                BigDecimal.valueOf(Short.MAX_VALUE, 2),
                BigDecimal.valueOf(Integer.MIN_VALUE, 3),
                BigDecimal.valueOf(Integer.MAX_VALUE, 4),
                BigDecimal.valueOf(Long.MIN_VALUE, 5),
                BigDecimal.valueOf(Long.MAX_VALUE, 6),
                new BigDecimal("1.23"),
                new BigDecimal("123456789.0123456789"),
                new BigDecimal("12345678901234567890123456789012345678901234567890123456789012345678901234567890"),
                new BigDecimal("1234567890.1234567890123456789012345678901234567890123456789012345678901234567890"),
        };
        for (BigDecimal i : numbers) {
            byte[] jsonbBytes = JSONB.toBytes(i);
            assertEquals(i.toString(), JSONB.parseObject(jsonbBytes, String.class));
        }
    }

    @Test
    public void test_naming() {
        SymbolTable symbolTable = JSONB.symbolTable("id");
        JSONWriter jsonWriter = JSONWriter.ofJSONB(symbolTable);
        jsonWriter.writeAny(
                Collections.singletonMap("id", 123)
        );
        byte[] jsonbBytes = jsonWriter.getBytes();
        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length, symbolTable);
        Map map = (Map) jsonReader.readAny();
        assertEquals(123, ((Number) map.get("id")).intValue());

        assertEquals("id", symbolTable.getNameByHashCode(Fnv.hashCode64("id")));
        assertNull(symbolTable.getNameByHashCode(-1));
        assertEquals(-1, symbolTable.getOrdinalByHashCode(-1));
    }

    @Test
    public void testWriteInt() {
        int[] values = new int[]{
                0,
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                Integer.MAX_VALUE,
                -1,
                -10,
                -100,
                -1000,
                -10000,
                -100000,
                -1000000,
                -10000000,
                -100000000,
                -1000000000,
                Integer.MIN_VALUE
        };

        for (int value : values) {
            byte[] bytes = JSONB.toBytes(value);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length);
            Number n = (Number) jsonReader.readAny();
            assertEquals(value, n.intValue());
        }

        for (int value : values) {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeAny(value);
            byte[] bytes = jsonWriter.getBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length);
            Number n = (Number) jsonReader.readAny();
            assertEquals(value, n.intValue());
        }
    }

    @Test
    public void testWriteLong() {
        long[] values = new long[]{
                0,
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L,
                9,
                99,
                999,
                9999,
                99999,
                999999,
                9999999,
                99999999,
                999999999,
                9999999999L,
                99999999999L,
                999999999999L,
                9999999999999L,
                99999999999999L,
                999999999999999L,
                9999999999999999L,
                99999999999999999L,
                999999999999999999L,
                Long.MAX_VALUE
                        - 1,
                -10,
                -100,
                -1000,
                -10000,
                -100000,
                -1000000,
                -10000000,
                -100000000,
                -1000000000,
                -10000000000L,
                -100000000000L,
                -1000000000000L,
                -10000000000000L,
                -100000000000000L,
                -1000000000000000L,
                -10000000000000000L,
                -100000000000000000L,
                -1000000000000000000L,
                -9,
                -99,
                -999,
                -9999,
                -99999,
                -999999,
                -9999999,
                -99999999,
                -999999999,
                -9999999999L,
                -99999999999L,
                -999999999999L,
                -9999999999999L,
                -99999999999999L,
                -999999999999999L,
                -9999999999999999L,
                -99999999999999999L,
                -999999999999999999L,
                Long.MIN_VALUE
        };

        for (long value : values) {
            byte[] bytes = JSONB.toBytes(value);
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length);
            Number n = (Number) jsonReader.readAny();
            assertEquals(value, n.longValue());
        }

        for (long value : values) {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeAny(value);
            byte[] bytes = jsonWriter.getBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length);
            Number n = (Number) jsonReader.readAny();
            assertEquals(value, n.longValue());
        }
    }

    @Test
    public void testWriteStr() {
        byte[] bytes = new byte[1024 * 1024 * 64];
        Arrays.fill(bytes, (byte) '0');
        for (int i = 1; i < bytes.length; i *= 10) {
            String str = new String(bytes, 0, i);
            byte[] strBytes = JSONB.toBytes(str);
            JSONReader jsonReader = JSONReader.ofJSONB(strBytes, 0, strBytes.length);
            assertEquals(str, jsonReader.readString());
        }

        for (int i = 1; i < bytes.length; i *= 10) {
            String str = new String(bytes, 0, i);
            byte[] strBytes = JSONB.toBytes(str, StandardCharsets.UTF_16);
            JSONReader jsonReader = JSONReader.ofJSONB(strBytes, 0, strBytes.length);
            assertEquals(str, jsonReader.readString());
        }

        for (int i = 1; i < bytes.length; i *= 10) {
            String str = new String(bytes, 0, i);
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeAny(str);
            byte[] strBytes = jsonWriter.getBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(strBytes, 0, strBytes.length);
            assertEquals(str, jsonReader.readString());
        }
    }

    @Test
    public void testToBytesNull() {
        assertArrayEquals(new byte[]{JSONB.Constants.BC_NULL},
                JSONB.toBytes((String) null));
        assertArrayEquals(new byte[]{JSONB.Constants.BC_NULL},
                JSONB.toBytes((String) null, (Charset) null));
        assertArrayEquals(new byte[]{JSONB.Constants.BC_NULL},
                JSONB.toBytes((BigInteger) null));
        assertArrayEquals(new byte[]{JSONB.Constants.BC_NULL},
                JSONB.toBytes((BigDecimal) null));
    }

    @Test
    public void testWriteBinaryCapacity() {
        byte[] bytes = new byte[1024 * 8];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }

        Arrays.fill(bytes, Byte.MIN_VALUE);
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        Arrays.fill(bytes, Byte.MAX_VALUE);
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void testWriteInt32Capacity() {
        int[] bytes = new int[1024 * 8];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }

        Arrays.fill(bytes, Byte.MIN_VALUE);
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }
        Arrays.fill(bytes, Byte.MAX_VALUE);
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void testWriteInt32Capacity_1() {
        short[] bytes = new short[1024 * 8];
        {
            Arrays.fill(bytes, Short.MIN_VALUE);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            short[] bytes2 = JSONB.parseObject(jsonbBytes, short[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, Short.MAX_VALUE);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            short[] bytes2 = JSONB.parseObject(jsonbBytes, short[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void testWriteInt32Capacity_2() {
        int[] bytes = new int[1024 * 8];
        {
            Arrays.fill(bytes, INT24_MIN);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, INT24_MAX);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }

        {
            Arrays.fill(bytes, Integer.MIN_VALUE);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, Integer.MAX_VALUE);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            int[] bytes2 = JSONB.parseObject(jsonbBytes, int[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void testWriteMapNullCapacity() {
        HashMap map = new HashMap(1024 * 8);
        for (int i = 0; i < 1024 * 8; ++i) {
            map.put(Integer.toString(i), null);
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(map);
            HashMap o = JSONB.parseObject(jsonbBytes, HashMap.class);
            assertTrue(o.isEmpty());
        }

        byte[] jsonbBytes = JSONB.toBytes(map, JSONWriter.Feature.WriteNulls);
        HashMap map1 = JSONB.parseObject(jsonbBytes, HashMap.class);
        assertEquals(map.size(), map1.size());
        assertEquals(map, map1);
    }

    @Test
    public void testWriteMap() {
        HashMap map = new HashMap(1024 * 8);
        for (int i = 0; i < 1024 * 8; ++i) {
            map.put(Integer.toString(i), i);
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(map);
            HashMap map1 = JSONB.parseObject(jsonbBytes, HashMap.class);
            assertEquals(map.size(), map1.size());
            assertEquals(map, map1);
        }

        String str = JSON.toJSONString(map);
        JSONReader[] jsonReaders4 = TestUtils.createJSONReaders4(str);
        for (int i = 0; i < jsonReaders4.length; i++) {
            JSONReader jsonReader = jsonReaders4[i];
            Map<String, Object> map1 = jsonReader.readObject();
            assertEquals(map.size(), map1.size());
            assertEquals(map, map1);
        }
    }

    @Test
    public void test_writeTo() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSONB.writeTo(out, Collections.singleton(1));
        assertEquals("[1]",
                JSON.toJSONString(JSONB.parse(out.toByteArray())));
    }

    @Test
    public void test_dup_bytes() {
        byte[] bytes = new byte[1024];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, (byte) 1);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void test_dup_bytes_4() {
        byte[] bytes = new byte[4];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, (byte) 1);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void test_dup_bytes_8() {
        byte[] bytes = new byte[8];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, (byte) 1);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void test_dup_bytes_16() {
        byte[] bytes = new byte[16];
        {
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            Arrays.fill(bytes, (byte) 1);
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void test_bytes_4_8_16_32() {
        {
            byte[] bytes = new byte[4];
            bytes[0] = 1;
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            byte[] bytes = new byte[8];
            bytes[0] = 1;
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            byte[] bytes = new byte[16];
            bytes[0] = 1;
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
        {
            byte[] bytes = new byte[32];
            bytes[0] = 1;
            byte[] jsonbBytes = JSONB.toBytes(bytes);
            byte[] bytes2 = JSONB.parseObject(jsonbBytes, byte[].class);
            assertArrayEquals(bytes, bytes2);
        }
    }

    @Test
    public void test_bool_array() {
        boolean[] array = new boolean[2048];
        byte[] jsonbBytes = JSONB.toBytes(array);
        boolean[] array2 = JSONB.parseObject(jsonbBytes, boolean[].class);
        assertArrayEquals(array, array2);
    }

    @Test
    public void test_object_null_array() {
        Object[] array = new Object[2048];
        byte[] jsonbBytes = JSONB.toBytes(array);
        Object[] array2 = JSONB.parseObject(jsonbBytes, Object[].class);
        assertArrayEquals(array, array2);
    }

    @Test
    public void test_object_array_null() {
        Object[] array = null;
        byte[] jsonbBytes = JSONB.toBytes(array);
        Object[] array2 = JSONB.parseObject(jsonbBytes, Object[].class);
        assertArrayEquals(array, array2);
    }

    @Test
    public void test_integer_null_array() {
        Integer[] array = new Integer[2048];
        byte[] jsonbBytes = JSONB.toBytes(array);
        Object[] array2 = JSONB.parseObject(jsonbBytes, Object[].class);
        assertArrayEquals(array, array2);
    }

    @Test
    public void test_integer_array_null() {
        Integer[] array = null;
        byte[] jsonbBytes = JSONB.toBytes(array);
        Integer[] array2 = JSONB.parseObject(jsonbBytes, Integer[].class);
        assertArrayEquals(array, array2);
    }

    @Test
    public void test_largeInput() {
        byte[] bytes = {(byte) 0xa6, 0x79, 0x48, 0x7f, 0x7f, 0x7f, 0x7f};
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes));
    }
}
