package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTypedMultiTest {
    JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks", "date", "2017-07-14");

    @Test
    public void test() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.id", "$.name", "$.date"},
                new Type[]{Long.class, String.class, Date.class}
        );
        assertTrue(jsonPath.isRef());
        assertFalse(jsonPath.isPrevious());
        assertFalse(jsonPath.contains(JSONObject.of()));
        assertTrue(jsonPath.contains(object));

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(object);
            assertArrayEquals(expected, result);
        }

        String jsonStr = object.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
        assertEquals(
                "[1001,\"DataWorks\",\"2017-07-14 00:00:00\"]",
                jsonPath.extractScalar(JSONReader.of(object.toString()))
        );
    }

    @Test
    public void test1() {
        JSONArray array = JSONArray.of(object);

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0].id", "$[0].name", "$[0].date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(array);
            assertArrayEquals(expected, result);
        }

        String jsonStr = array.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test1a() {
        JSONArray array = JSONArray.of(null, object);

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[1].id", "$[1].name", "$[1].date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(array);
            assertArrayEquals(expected, result);
        }

        String jsonStr = array.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test2() {
        JSONObject root = JSONObject.of("value", object);

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.value.id", "$.value.name", "$.value.date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(root);
            assertArrayEquals(expected, result);
        }

        String jsonStr = root.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test3() {
        JSONObject root = JSONObject.of("value", JSONObject.of("item", object));

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.value.item.id", "$.value.item.name", "$.value.item.date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(root);
            assertArrayEquals(expected, result);
        }

        String jsonStr = root.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test4() {
        JSONArray root = JSONArray.of(JSONArray.of(object));

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$[0][0].id", "$[0][0].name", "$[0][0].date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(root);
            assertArrayEquals(expected, result);
        }

        String jsonStr = root.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test5() {
        JSONObject root = JSONObject.of("id", 1001, "values", JSONArray.of(object));

        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.id", "$.values[0].name", "$.values[0].date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(root);
            assertArrayEquals(expected, result);
        }

        String jsonStr = root.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
    }

    @Test
    public void test6() {
        JSONPathTypedMulti jsonPath = (JSONPathTypedMulti) JSONPath.of(
                new String[]{"$.id", "$.values[0].name", "$.values[0].date"},
                new Type[]{Long.class, String.class, Date.class}
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), new Object())
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.set(new Object(), new Object(), JSONReader.Feature.ErrorOnEnumNotMatch)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setCallback(new Object(), (BiFunction) null)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setInt(new Object(), 1)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.setLong(new Object(), 1)
        );
        assertThrows(
                JSONException.class,
                () -> jsonPath.remove(new Object())
        );
    }

    @Test
    public void test7() {
        JSONPathTypedMulti jsonPath = (JSONPathTypedMulti) JSONPath.of(
                new String[]{"$", "$.values[0].name", "$.values[0].date"},
                new Type[]{JSONObject.class, String.class, Date.class}
        );
        assertFalse(jsonPath.contains(object));
        assertTrue(jsonPath.isRef());
    }

    @Test
    public void test8() {
        JSONPathTypedMulti jsonPath = (JSONPathTypedMulti) JSONPath.of(
                new String[]{"$.id.abs()", "$.values[0].name", "$.values[0].date"},
                new Type[]{Integer.class, String.class, Date.class}
        );
        assertTrue(jsonPath.contains(object));
        assertFalse(jsonPath.isRef());
    }
}
