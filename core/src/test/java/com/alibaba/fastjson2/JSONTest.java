package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.alibaba.fastjson2.reader.ObjectReaderImplList;
import com.alibaba.fastjson2.reader.ObjectReaderImplListStr;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2_vo.IntField1;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTest {
    @Test
    public void test_parseObject_0() {
        IntField1 vo = JSON.parseObject("{\"v0000\":101}"
                , (Type) IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_isValidArray_0() {
        assertTrue(JSON.isValidArray("[]"));
        assertFalse(JSON.isValidArray("{}"));
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
    public void test_parseObject_2() {
        IntField1 vo = JSON.parseObject("{\"v0000\":101}".getBytes(StandardCharsets.UTF_8)
                , (Type) IntField1.class);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_1() {
        IntField1 vo = JSON.parseObject("{\"v0000\":101}".getBytes(StandardCharsets.UTF_8)
                , (Type) IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_inputStream() {
        byte[] bytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 vo = JSON.parseObject(new ByteArrayInputStream(bytes)
                , IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_inputStream_charset() {
        byte[] bytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 vo = JSON.parseObject(new ByteArrayInputStream(bytes)
                , StandardCharsets.UTF_8
                , IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
    }

    @Test
    public void test_parseObject_inputStream_charset1() {
        byte[] bytes = "{\"v0000\":101}".getBytes(StandardCharsets.UTF_8);
        IntField1 vo = JSON.parseObject(new ByteArrayInputStream(bytes)
                , null
                , IntField1.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals(101, vo.v0000);
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
    public void test_null() {
        assertNull(JSON.parse("null"));
    }

    @Test
    public void test_writeNull() {
        assertEquals("null"
                , JSON.toJSONString(null, JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void test_writeNull_utf8() {
        assertEquals("null"
                , new String(JSON.toJSONBytes(null, JSONWriter.Feature.WriteNulls)));
    }

    @Test
    public void test_writeTo_0() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out, Collections.singleton(1));
        assertEquals("[1]"
                , new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_1() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                null, new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("null"
                , new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_2() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                Collections.singleton(1), new Filter[0], JSONWriter.Feature.WriteNulls);
        assertEquals("[1]"
                , new String(out.toByteArray()));
    }

    @Test
    public void test_writeTo_3() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JSON.writeTo(out,
                Collections.singleton(1), Arrays.asList(new SimplePropertyPreFilter()).toArray(new Filter[0]), JSONWriter.Feature.WriteNulls);
        assertEquals("[1]"
                , new String(out.toByteArray()));
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
                1
                , 10
                , 100
                , 1000
                , 10000
                , 100000
                , 1000000
                , 10000000
                , 100000000
                , 1000000000
                , 1000000000
        };
        for (int i : numbers) {
            assertEquals(i, JSON.parse(Integer.toString(i)));
        }
    }

    @Test
    public void test_num_long() {
        long[] numbers = new long[]{
                10000000000L
                , 100000000000L
                , 1000000000000L
                , 10000000000000L
                , 100000000000000L
                , 1000000000000000L
                , 10000000000000000L
                , 100000000000000000L
                , 1000000000000000000L
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

        assertEquals(123
                , ((List) JSON.parseObject("\"123\""
                        , new TypeReference<List<Integer>>() {
                        }.getType()))
                        .get(0));
        assertEquals(123
                , ((List) JSON.parseObject("\"123\""
                        , new TypeReference<LinkedList<Integer>>() {
                        }.getType()))
                        .get(0));
        assertEquals(123
                , ((List) JSON.parseObject("\"123\""
                        , new TypeReference<ArrayList<Integer>>() {
                        }.getType()))
                        .get(0));
        assertEquals(123
                , ((List) JSON.parseObject("\"123\""
                        , new TypeReference<AbstractList<Integer>>() {
                        }.getType()))
                        .get(0));

        new ObjectReaderImplList(MyList.class, MyList.class, MyList.class, Integer.class, null).createInstance();

        Object instance = new ObjectReaderImplList(MyList0.class, MyList0.class, MyList0.class, Integer.class, null).createInstance();
        assertNotNull(instance);
    }

    @Test
    public void test_list_str_0() {


        assertEquals("123"
                , ((List) JSON.parseObject("[\"123\"]"
                        , new TypeReference<List<String>>() {
                        }.getType()))
                        .get(0));
        assertEquals("123"
                , ((List) JSON.parseObject("[\"123\"]"
                        , new TypeReference<LinkedList<String>>() {
                        }.getType()))
                        .get(0));
        assertEquals("123"
                , ((List) JSON.parseObject("[\"123\"]"
                        , new TypeReference<ArrayList<String>>() {
                        }.getType()))
                        .get(0));
        assertEquals("123"
                , ((List) JSON.parseObject("[\"123\"]"
                        , new TypeReference<AbstractList<String>>() {
                        }.getType()))
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

    public static class MyList<T> extends ArrayList<T> {

    }

    private static class MyList0<T> extends ArrayList<T> {

    }
}
