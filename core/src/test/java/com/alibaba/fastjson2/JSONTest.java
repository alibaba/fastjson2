package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PascalNameFilter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.ObjectReaderImplList;
import com.alibaba.fastjson2.reader.ObjectReaderImplListStr;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2_vo.Date1;
import com.alibaba.fastjson2_vo.IntField1;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTest {
    @Test
    public void test_parseStringOffset() {
        String str = "a {\"id\":123} b";
        {
            JSONObject object = (JSONObject) JSON.parse(str, 2, str.length() - 4);
            assertEquals(123, object.get("id"));
        }
        {
            JSONObject object = JSON.parseObject(str, 2, str.length() - 4);
            assertEquals(123, object.get("id"));
        }
        {
            JSONObject object = JSON.parseObject(str, 2, str.length() - 4, JSONObject.class);
            assertEquals(123, object.get("id"));
        }
    }

    @Test
    public void test_isValidArray_0() {
        assertTrue(JSON.isValidArray("[]"));
        assertFalse(JSON.isValidArray("{}"));
    }

    @Test
    public void isValid() {
        assertFalse(JSON.isValid((char[]) null));
        assertFalse(JSON.isValid(new char[0]));
        assertFalse(JSON.isValid("{}1".toCharArray()));
    }

    @Test
    public void test_parseArray_0() {
        String str = "[1,2,3]";
        List<Object> array = JSON.parseArray(str, new Type[]{int.class, long.class, String.class});
        assertEquals(1, array.get(0));
        assertEquals(2L, array.get(1));
        assertEquals("3", array.get(2));
    }

    @Test
    public void test_parseObject_0() {
        IntField1 intField1 = JSON.parseObject("{\"v0000\":101}",
                (Type) IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, intField1.v0000);
        Date1 date1 = JSON.parseObject("{\"date\":\"20180131022733000-0800\"}",
                (Type) Date1.class, "yyyyMMddHHmmssSSSZ", JSONReader.Feature.FieldBased);
        assertNotNull(date1.getDate());
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"date\":\"20180131022733000-0800\"}",
                (Type) Date1.class, ""));
    }

    @Test
    public void test_parseObject_2() {
        IntField1 vo = JSON.parseObject("{\"v0000\":101}".getBytes(StandardCharsets.UTF_8),
                (Type) IntField1.class);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_1() {
        IntField1 intField1 = JSON.parseObject("{\"v0000\":101}".getBytes(StandardCharsets.UTF_8),
                (Type) IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, intField1.v0000);
        Date1 date1 = JSON.parseObject("{\"date\":\"20180131022733000-0800\"}".getBytes(StandardCharsets.UTF_8),
                (Type) Date1.class, "yyyyMMddHHmmssSSSZ", JSONReader.Feature.FieldBased);
        assertNotNull(date1.getDate());
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"date\":\"20180131022733000-0800\"}".getBytes(StandardCharsets.UTF_8),
                (Type) Date1.class, ""));
    }

    @Test
    public void test_parseObject_inputStream() {
        byte[] intBytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 intField1 = JSON.parseObject(new ByteArrayInputStream(intBytes),
                IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, intField1.v0000);
        byte[] dateBytes = "{\"date\":\"20180131022733000-0800\"}".getBytes(StandardCharsets.UTF_8);
        Date1 date1 = JSON.parseObject(new ByteArrayInputStream(dateBytes),
                Date1.class, "yyyyMMddHHmmssSSSZ", JSONReader.Feature.FieldBased);
        assertNotNull(date1.getDate());
        assertThrows(JSONException.class, () -> JSON.parseObject(new ByteArrayInputStream(dateBytes),
                Date1.class, ""));
    }

    @Test
    public void test_parseObject_inputStream_charset() {
        byte[] bytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 vo = JSON.parseObject(new ByteArrayInputStream(bytes),
                StandardCharsets.UTF_8,
                IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_inputStream_charset1() {
        byte[] bytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 vo = JSON.parseObject(new ByteArrayInputStream(bytes),
                null,
                IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_inputStream_consumer() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "{\"id\":1,\"name\":\"fastjson\"}\n{\"id\":2,\"name\":\"fastjson2\"}\n".getBytes(StandardCharsets.UTF_8)
        );

        JSON.parseObject(
                input, StandardCharsets.UTF_8,
                '\n', User.class, (Consumer<User>) user -> {
                    assertNotNull(user);
                    switch (user.id) {
                        case 1: {
                            assertEquals("fastjson", user.name);
                            break;
                        }
                        case 2: {
                            assertEquals("fastjson2", user.name);
                            break;
                        }
                    }
                });
    }

    @Test
    public void test_parseObject_3() {
        IntField1 vo = JSON.toJavaObject(JSON.parseObject("{\"v0000\":101}"), IntField1.class);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_4() {
        IntField1 vo = JSON.toJavaObject("{\"v0000\":101}", IntField1.class);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_toJSONBytes_0() {
        assertEquals("null", new String(JSON.toJSONBytes(null, new Filter[0])));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, null);
        assertEquals("null", new String(out.toByteArray()));
    }

    @Test
    public void test_toJSONBytes_1() {
        assertEquals("\"test\"",
                new String(JSON.toJSONBytes("test", new Filter[0], JSONWriter.Feature.WriteNulls)));
        assertEquals("\"test\"",
                new String(JSON.toJSONBytes("test", Arrays.asList(new SimplePropertyPreFilter()).toArray(new Filter[0]), JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_object_empty() {
        Map object = (Map) JSON.parse("{}");
        assertTrue(object.isEmpty());
    }

    @Test
    public void test_object_one() {
        Map object = (Map) JSON.parse("{\"id\":123}");
        assertEquals(1, object.size());
        assertEquals(123, object.get("id"));
    }

    @Test
    public void test_parse_object_typed_empty() {
        Type[] types = new Type[]{
                Object.class,
                Cloneable.class,
                Closeable.class,
                Serializable.class
        };
        for (Type type : types) {
            Map object = JSON.parseObject("{}", type);
            assertTrue(object.isEmpty());
        }
    }

    @Test
    public void test_parse_object_empty() {
        Map object = JSON.parseObject("{}");
        assertTrue(object.isEmpty());
    }

    @Test
    public void test_parse_object_one() {
        Map object = JSON.parseObject("{\"id\":123}");
        assertEquals(1, object.size());
        assertEquals(123, object.get("id"));
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class, String.class}, null, type);

            Map object = JSON.parseObject("{\"id\":123}", parameterizedType);
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class, Long.class}, null, type);

            Map object = JSON.parseObject("{\"id\":123}", parameterizedType);
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

        for (Type type : types) {
            List list = (List) JSON.parseObject("[123]", type);
            assertEquals(1, list.size());
            assertEquals(123, list.get(0));
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{}, null, type);
            List list = JSON.parseObject("[123]", parameterizedType);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get());
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            List list = JSON.parseObject("[123]", parameterizedType);
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            List list = JSON.parseObject("[123]", parameterizedType);
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

        for (Type type : types) {
            Collection list = (Collection) JSON.parseObject("[123]", type);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get());
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            Collection list = JSON.parseObject("[123]", parameterizedType);
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            Collection list = JSON.parseObject("[123]", parameterizedType);
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

        for (Type type : types) {
            Set list = (Set) JSON.parseObject("[123]", type);
            assertEquals(1, list.size());
            assertEquals(123, list.stream().findFirst().get());
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{String.class}, null, type);
            Set list = (Set) JSON.parseObject("[123]", parameterizedType);
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

        for (Type type : types) {
            ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(new Type[]{Long.class}, null, type);
            Set list = (Set) JSON.parseObject("[123]", parameterizedType);
            assertEquals(1, list.size());
            assertEquals(123L, list.stream().findFirst().get());
        }
    }

    @Test
    public void test_array_empty() {
        List list = (List) JSON.parse("[]");
        assertTrue(list.isEmpty());
    }

    @Test
    public void test_array_one() {
        List list = (List) JSON.parse("[123]");
        assertEquals(1, list.size());
        assertEquals(123, list.get(0));
    }

    @Test
    public void test_parse_array_empty() {
        List list = JSON.parseArray("[]");
        assertTrue(list.isEmpty());
    }

    @Test
    public void test_parse_array_one() {
        List list = JSON.parseArray("[123]");
        assertEquals(1, list.size());
        assertEquals(123, list.get(0));
    }

    @Test
    public void test_parse_array_typed() {
        List<String> list = JSON.parseArray("[123]", String.class);
        assertEquals(1, list.size());
        assertEquals("123", list.get(0));
    }

    @Test
    public void test_to_json_string_with_format() {
        java.util.Date date = JSON.parseObject("\"2017-3-17 00:00:01\"", java.util.Date.class);
        String json1 = JSON.toJSONString(date, "yyyy-MM-dd", new Filter[0], JSONWriter.Feature.FieldBased);
        assertEquals("\"2017-03-17\"", json1);
        String json2 = JSON.toJSONString(date, "", new Filter[0], JSONWriter.Feature.FieldBased);
        assertEquals("\"2017-03-17 00:00:01\"", json2);
    }

    @Test
    public void test_to_json_bytes_with_format() {
        java.util.Date date = JSON.parseObject("\"2017-3-17 00:00:01\"", java.util.Date.class);
        byte[] bytes1 = JSON.toJSONBytes(date, "yyyy-MM-dd", new Filter[0], JSONWriter.Feature.FieldBased);
        java.util.Date date1 = JSON.parseObject(bytes1, java.util.Date.class);
        assertNotNull(date1);
        byte[] bytes2 = JSON.toJSONBytes(date, "", new Filter[0], JSONWriter.Feature.FieldBased);
        java.util.Date date2 = JSON.parseObject(bytes2, java.util.Date.class);
        assertNotNull(date2);
    }

    @Test
    public void test_null() {
        assertNull(JSON.parse("null"));
        assertNull(JSON.parse(""));
        assertNull(JSON.parse(null, 0, 0));
        assertNull(JSON.parse("", 0, 0));
        assertNull(JSON.parse("abc", 0, 0));

        assertNull(JSON.parseObject("null"));
        assertNull(JSON.parseObject(""));

        assertNull(JSON.parseObject("null", JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject("", JSONReader.Feature.SupportSmartMatch));

        assertNull(JSON.parseObject("null", JSONFactory.createReadContext()));
        assertNull(JSON.parseObject("null", JSONFactory.createReadContext(JSONReader.Feature.SupportSmartMatch)));
        assertNull(JSON.parseObject("", JSONFactory.createReadContext()));
        assertEquals("{}", JSON.parseObject("{}", JSONFactory.createReadContext()).toString());

        assertNull(JSON.parseObject((String) null, 0, 0));
        assertNull(JSON.parseObject("", 0, 0));
        assertNull(JSON.parseObject("abc", 0, 0));
        assertNull(JSON.parseObject("null", 0, 4));

        assertNull(JSON.parseObject((byte[]) null));
        assertNull(JSON.parseObject(new byte[0]));
        assertNull(JSON.parseObject("null".getBytes(StandardCharsets.UTF_8)));

        assertNull(JSON.parseObject((byte[]) null, 0, 0));
        assertNull(JSON.parseObject(new byte[0], 0, 0));
        assertNull(JSON.parseObject("abc".getBytes(StandardCharsets.UTF_8), 0, 0));
        assertNull(JSON.parseObject("null".getBytes(StandardCharsets.UTF_8), 0, 4));

        assertNull(JSON.parseObject((byte[]) null, 0, 0, StandardCharsets.US_ASCII));
        assertNull(JSON.parseObject(new byte[0], 0, 0, StandardCharsets.US_ASCII));
        assertNull(JSON.parseObject("abc".getBytes(StandardCharsets.UTF_8), 0, 0, StandardCharsets.US_ASCII));
        assertNull(JSON.parseObject("null".getBytes(StandardCharsets.UTF_8), 0, 4, StandardCharsets.US_ASCII));

        assertNull(JSON.parseObject((String) null, Object.class, (Filter) null));
        assertNull(JSON.parseObject("", Object.class, (Filter) null));
        assertNull(JSON.parseObject("null", Object.class, (Filter) null));

        assertNull(JSON.parseObject((String) null, Object.class));
        assertNull(JSON.parseObject("", Object.class));
        assertNull(JSON.parseObject("null", Object.class));

        assertNull(JSON.parseObject((String) null, (Type) Object.class));
        assertNull(JSON.parseObject("", (Type) Object.class));
        assertNull(JSON.parseObject("null", (Type) Object.class));

        assertNull(JSON.parseObject((String) null, 0, 0, Object.class));
        assertNull(JSON.parseObject("", 0, 0, Object.class));
        assertNull(JSON.parseObject("null", 0, 0, Object.class));

        assertNull(JSON.parseObject((String) null, Object.class, "", new Filter[0]));
        assertNull(JSON.parseObject("", Object.class, "", new Filter[0]));
        assertNull(JSON.parseObject("null", Object.class, "", new Filter[0]));

        assertNull(JSON.parseObject((byte[]) null, Object.class, "", new Filter[0]));
        assertNull(JSON.parseObject("".getBytes(StandardCharsets.UTF_8), Object.class, "", new Filter[0]));
        assertNull(JSON.parseObject("null".getBytes(StandardCharsets.UTF_8), Object.class, "", new Filter[0]));

        assertNull(JSON.parseObject((String) null, new TypeReference<List<Map>>(){}, (Filter) null));
        assertNull(JSON.parseObject("", new TypeReference<List<Map>>(){}, (Filter) null));
        assertNull(JSON.parseObject("null", new TypeReference<List<Map>>(){}, (Filter) null));

        assertNull(JSON.parseArray((byte[]) null, (Type) Object.class));
        assertNull(JSON.parseArray(new byte[0], (Type) Object.class));
        assertNull(JSON.parseArray("null".getBytes(StandardCharsets.UTF_8), (Type) Object.class));

        assertNull(JSON.parseArray("null", new Type[0]));

        assertNull(JSON.parseObject((byte[]) null, Object.class, (Filter) null));
        assertNull(JSON.parseObject(new byte[0], Object.class, (Filter) null));
        assertNull(JSON.parseObject("null".getBytes(StandardCharsets.UTF_8), Object.class, (Filter) null));
    }

    @Test
    public void test_writeNull() {
        assertEquals("null",
                JSON.toJSONString(null, JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void test_writeNull_utf8() {
        assertEquals("null",
                new String(JSON.toJSONBytes(null, JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeTo_0() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, Collections.singleton(1));
        assertEquals("[1]",
                new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_1() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                null, new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("null",
                new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_2() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                Collections.singleton(1), new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("[1]",
                new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_3() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                Collections.singleton(1), Arrays.asList(new SimplePropertyPreFilter()).toArray(new Filter[0]), JSONWriter.Feature.WriteNulls);
        assertEquals("[1]",
                new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_4() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                Collections.singleton(1), "millis", new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("[1]",
                new String(out.toByteArray()));
        JSON.writeTo(out,
                Collections.singleton(1), "", new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("[1][1]",
                new String(out.toByteArray()));
    }

    @Test
    public void test_true() {
        assertTrue((Boolean) JSON.parse("true"));
    }

    @Test
    public void test_false() {
        assertFalse((Boolean) JSON.parse("false"));
    }

    @Test
    public void test_str() {
        String str = "wenshao";
        assertEquals(str, JSON.parse("\"" + str + "\""));
    }

    @Test
    public void test_num_1() {
        assertEquals(0, JSON.parse("0"));
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
            assertEquals(i, JSON.parse(Integer.toString(i)));
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
            assertEquals(i, JSON.parse(Long.toString(i)));
        }
    }

    @Test
    public void test_list_0() {
        assertNull(ObjectReaderImplList.INSTANCE.getFieldReader(0));
        ObjectReaderImplList.INSTANCE.getObjectClass();
        assertEquals(Fnv.hashCode64("@type"), ObjectReaderImplList.INSTANCE.getTypeKeyHash());

        assertEquals(123,
                ((List) JSON.parseObject("\"123\"",
                        new TypeReference<List<Integer>>() {}.getType())
                ).get(0));
        assertEquals(123,
                ((List) JSON.parseObject("\"123\"",
                        new TypeReference<LinkedList<Integer>>() {}.getType()))
                        .get(0));
        assertEquals(123,
                ((List) JSON.parseObject("\"123\"",
                        new TypeReference<ArrayList<Integer>>() {}.getType()))
                        .get(0));
        assertEquals(123,
                ((List) JSON.parseObject("\"123\"",
                        new TypeReference<AbstractList<Integer>>() {}.getType()))
                        .get(0));

        new ObjectReaderImplList(MyList.class, MyList.class, MyList.class, Integer.class, null).createInstance();

        Object instance = new ObjectReaderImplList(MyList0.class, MyList0.class, MyList0.class, Integer.class, null).createInstance();
        assertNotNull(instance);
    }

    @Test
    public void test_list_str_0() {
        assertEquals("123",
                ((List) JSON.parseObject("[\"123\"]",
                        new TypeReference<List<String>>() {}.getType()))
                        .get(0));
        assertEquals("123",
                ((List) JSON.parseObject("[\"123\"]",
                        new TypeReference<LinkedList<String>>() {}.getType()))
                        .get(0));
        assertEquals("123",
                ((List) JSON.parseObject("[\"123\"]",
                        new TypeReference<ArrayList<String>>() {}.getType()))
                        .get(0));
        assertEquals("123",
                ((List) JSON.parseObject("[\"123\"]",
                        new TypeReference<AbstractList<String>>() {}.getType()))
                        .get(0));

        new ObjectReaderImplListStr(MyList.class, MyList.class).createInstance();
        assertNull(new ObjectReaderImplListStr(MyList.class, MyList.class).getFieldReader(0));

        Exception error = null;
        try {
            new ObjectReaderImplListStr(MyList.class, MyList0.class).createInstance();
        } catch (JSONException ex) {
            error = ex;
        }
        assertNotNull(error);
    }

    @Test
    public void test_parseArray_bytes() {
        String text = "[{\"id\":1,\"name\":\"kraity\"}]";
        byte[] data = text.getBytes(StandardCharsets.UTF_8);

        List<User> users = JSON.parseArray(data, User.class);
        assertNotNull(users);
        assertEquals(1, users.size());

        User user = users.get(0);
        assertNotNull(user);
        assertEquals(1, user.id);
        assertEquals("kraity", user.name);
    }

    public static class User {
        public int id;
        public String name;
    }

    public static class MyList<T>
            extends ArrayList<T> {
    }

    private static class MyList0<T>
            extends ArrayList<T> {
    }

    @Test
    public void testNulls() {
        assertNull(JSON.parse(null));
        assertNull(JSON.parse((byte[]) null, JSONReader.Feature.SupportAutoType));
        assertNull(JSON.parse((String) null, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((String) null));
        assertNull(JSON.parseObject((String) null, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((byte[]) null));
        assertNull(JSON.parseObject((byte[]) null, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject(new byte[0]));
        assertNull(JSON.parseObject(new byte[0], JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((byte[]) null, User.class));
        assertNull(JSON.parseObject((byte[]) null, User.class, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((byte[]) null, (Type) User.class));
        assertNull(JSON.parseObject((byte[]) null, (Type) User.class, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((byte[]) null, (Type) User.class, ""));
        assertNull(JSON.parseObject((byte[]) null, (Type) User.class, "", JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject(new byte[0], (Type) User.class, ""));
        assertNull(JSON.parseObject(new byte[0], (Type) User.class, "", JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject(new byte[0], 0, 0, StandardCharsets.UTF_8, (Type) User.class));

        assertNull(JSON.parseObject(new char[0], 0, 0, User.class));
        assertNull(JSON.parseObject(new char[0], 0, 0));
        assertNull(JSON.parseObject(new char[0]));
        assertNull(JSON.parseArray(new char[0]));
        assertNull(JSON.parseArray(new char[0], User.class));

        assertNull(JSON.parseObject((char[]) null, 0, 0, User.class));
        assertNull(JSON.parseObject((char[]) null, 0, 0));
        assertNull(JSON.parseObject((char[]) null));
        assertNull(JSON.parseObject("null".toCharArray()));
        assertNull(JSON.parseObject("null".toCharArray(), User.class));
        assertNull(JSON.parseObject((char[]) null, User.class));
        assertNull(JSON.parseObject(new char[0], User.class));
        assertNull(JSON.parseArray((char[]) null));
        assertNull(JSON.parseArray((char[]) null, User.class));
        assertNull(JSON.parseArray(new char[0], User.class));
        assertNull(JSON.parseObject("null".toCharArray(), 0, "null".length()));

        assertNull(JSON.parseObject((byte[]) null, 0, 0, User.class));
        assertNull(JSON.parseObject((byte[]) null, 0, 0));
        assertNull(JSON.parseObject((byte[]) null));
        assertNull(JSON.parseArray((byte[]) null));
        assertNull(JSON.parseArray((byte[]) null, User.class));
        assertNull(JSON.parseArray((URL) null));
        assertNull(JSON.parseObject((byte[]) null, User.class, JSONFactory.createReadContext()));
        assertNull(JSON.parseObject(new byte[0], User.class, JSONFactory.createReadContext()));
        assertNull(JSON.parseObject(new byte[0], 0, 0, StandardCharsets.UTF_8, User.class, JSONReader.Feature.IgnoreNoneSerializable));
        assertNull(JSON.parseObject((byte[]) null, 0, 0, StandardCharsets.UTF_8, User.class, JSONReader.Feature.IgnoreNoneSerializable));

        assertNull(JSON.parseObject(new ByteArrayInputStream("null".getBytes()), StandardCharsets.UTF_8));
        assertNull(JSON.parseObject("null".getBytes(), JSONReader.Feature.IgnoreNoneSerializable));
        assertNull(JSON.parseArray(new ByteArrayInputStream("null".getBytes()), JSONReader.Feature.IgnoreNoneSerializable));
        assertNull(JSON.parseArray("", (Type) User.class, JSONReader.Feature.IgnoreNoneSerializable));
        assertNull(JSON.parseArray((String) null, (Type) User.class, JSONReader.Feature.IgnoreNoneSerializable));
        assertNull(JSON.parseArray((byte[]) null, 0, 0, StandardCharsets.UTF_8, User.class, JSONReader.Feature.IgnoreNoneSerializable));

        assertNull(JSON.parseObject(null, 0, 0, StandardCharsets.UTF_8));
        assertNull(JSON.parseArray(null, 0, 0, StandardCharsets.UTF_8));
        assertNull(JSON.parseObject(null, 0, 0, StandardCharsets.UTF_8, User.class));
        assertNull(JSON.parseObject(new byte[0], 0, 0, StandardCharsets.UTF_8));
        assertNull(JSON.parseArray(new byte[0], 0, 0, StandardCharsets.UTF_8));
        assertNull(JSON.parseObject(new byte[0], 0, 0, StandardCharsets.UTF_8, User.class));

        assertNull(JSON.parseObject((String) null, User.class));
        assertNull(JSON.parseObject((String) null, User.class, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((String) null, (Type) User.class));
        assertNull(JSON.parseObject((String) null, (Type) User.class, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((String) null, User.class, ""));
        assertNull(JSON.parseObject((String) null, User.class, "", JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((String) null, (Type) User.class, ""));
        assertNull(JSON.parseObject((String) null, (Type) User.class, "", JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseObject((String) null, new TypeReference<List<User>>() {
        }));
        assertNull(JSON.parseObject((String) null, new TypeReference<List<User>>() {
        }, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseArray((String) null));
        assertNull(JSON.parseArray((String) null, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseArray(""));
        assertNull(JSON.parseArray("", JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseArray((String) null));
        assertNull(JSON.parseArray((String) null, JSONReader.Feature.SupportAutoType));

        assertNull(JSON.parseArray("", User.class));
        assertNull(JSON.parseArray("", User.class, JSONReader.Feature.SupportAutoType));
        assertNull(JSON.parseArray("", new Type[]{User.class}, JSONReader.Feature.SupportAutoType));

        assertEquals("null", JSON.toJSONString(null, (Filter) null));
        assertEquals("null", JSON.toJSONString(null, (Filter[]) null));
        assertEquals("null", JSON.toJSONString(null, ""));
        assertEquals("null", JSON.toJSONString(null, "", (Filter[]) null));

        assertEquals("null", new String(JSON.toJSONBytes(null, (Filter) null)));
        assertEquals("null", new String(JSON.toJSONBytes(null, (Filter[]) null)));
        assertEquals("null", new String(JSON.toJSONBytes(null, (Filter[]) null, JSONWriter.Feature.WriteNulls)));
        assertEquals("null", new String(JSON.toJSONBytes(null, "", (Filter[]) null)));

        assertFalse(JSON.isValid((String) null));
        assertFalse(JSON.isValid(""));
        assertFalse(JSON.isValid((byte[]) null));
        assertFalse(JSON.isValid(new byte[0]));
        assertFalse(JSON.isValid(new byte[0], 0, 0, StandardCharsets.US_ASCII));

        assertFalse(JSON.isValidArray((String) null));
        assertFalse(JSON.isValidArray(""));
        assertFalse(JSON.isValidArray((byte[]) null));
        assertFalse(JSON.isValidArray(new byte[0]));

        assertNull(JSON.toJSON(null));
        assertNull(JSON.toJavaObject(null, null));

        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JSON.writeTo(out, null, "", (Filter[]) null);
            assertEquals("null", new String(out.toByteArray()));
        }

        assertNull(JSON.parseObject((URL) null));
    }

    @Test
    public void test_writeTo() {
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JSON.writeTo(out, 12, "", new Filter[0]);
            assertEquals("12", new String(out.toByteArray()));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JSON.writeTo(out, 12, "", new Filter[]{new PascalNameFilter()});
            assertEquals("12", new String(out.toByteArray()));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream() {
                public void write(byte[] b, int off, int len) {
                    throw new UnsupportedOperationException();
                }
            };
            assertThrows(JSONException.class, () ->
                    JSON.writeTo(out, JSONObject.of("id", 123)));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertThrows(JSONException.class, () ->
                    JSON.writeTo(out, JSONObject.of("id", 123), new Filter[]{new NameFilter() {
                        @Override
                        public String process(Object object, String name, Object value) {
                            throw new UnsupportedOperationException();
                        }
                    }}));
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertThrows(JSONException.class, () ->
                    JSON.writeTo(out, JSONObject.of("id", 123), "", new Filter[]{new NameFilter() {
                        @Override
                        public String process(Object object, String name, Object value) {
                            throw new UnsupportedOperationException();
                        }
                    }}));
        }
    }

    @Test
    public void test_toJSONBytes() {
        JSON.toJSONBytes(123, "", new Filter[]{new PascalNameFilter()});
    }

    @Test
    public void test_toJSON() {
        JSONObject object = new JSONObject();
        assertSame(object, JSON.toJSON(object));

        JSONArray array = new JSONArray();
        assertSame(array, JSON.toJSON(array));

        assertEquals(1, JSON.toJSON(1));
    }

    @Test
    public void testValid() {
        assertTrue(JSON.isValidArray("[]".getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray("{}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testRegisterReaderModule() {
        ObjectReaderModule module = new ObjectReaderModule() {
            @Override
            public void init(ObjectReaderProvider provider) {
                ObjectReaderModule.super.init(provider);
            }
        };
        JSON.register(module);
        JSONFactory.getDefaultObjectReaderProvider().unregister(module);
    }

    @Test
    public void testRegisterWriterModule() {
        ObjectWriterModule module = new ObjectWriterModule() {
            @Override
            public void init(ObjectWriterProvider provider) {
                ObjectWriterModule.super.init(provider);
            }
        };
        JSON.register(module);
        JSONFactory.getDefaultObjectWriterProvider().unregister(module);
    }

    @Test
    public void testParseInputStream() throws Exception {
        Charset utf8 = StandardCharsets.UTF_8;

        assertNull(JSON.parseObject((InputStream) null, utf8));
        String str = "{}";

        byte[] bytes = str.getBytes(utf8);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        JSONObject object = JSON.parseObject(is, utf8);
        assertEquals(0, object.size());
    }

    @Test
    public void testConfig() {
        assertThrows(JSONException.class, () -> JSON.config(JSONReader.Feature.SupportAutoType));
        assertThrows(JSONException.class, () -> JSON.config(JSONReader.Feature.SupportAutoType, true));
        JSON.config(JSONReader.Feature.SupportAutoType, false);
        assertFalse(JSON.isEnabled(JSONReader.Feature.SupportAutoType));
    }

    @Test
    public void testFilter() {
        HashMap map = JSON.parseObject("{}", HashMap.class, null, new Filter[0]);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testFilter1() {
        HashMap map = JSON.parseObject("{}", (Type) HashMap.class, (Filter) null);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testFilter2() {
        HashMap map = JSON.parseObject("{}".getBytes(StandardCharsets.UTF_8), (Type) HashMap.class, (Filter) null);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }
}
