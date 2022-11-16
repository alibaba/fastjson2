package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class JSONPathTypedMultiTest {
    JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks", "date", "2017-07-14");

    @Test
    public void test() {
        JSONPath jsonPath = JSONPath.of(
                new String[]{"$.id", "$.name", "$.date"},
                new Type[]{Long.class, String.class, Date.class}
        );

        Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};
        {
            Object[] result = (Object[]) jsonPath.eval(object);
            assertArrayEquals(expected, result);
        }

        String jsonStr = object.toString();
        Object[] result = (Object[]) jsonPath.extract(jsonStr);
        assertArrayEquals(expected, result);
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
}
