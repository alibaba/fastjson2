package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("jsonpath")
public class JSONPathNewFeaturesTest {
    // ========== avg() ==========

    @Test
    public void testAvg() {
        JSONArray array = JSONArray.of(1, 2, 3, 4, 5);
        Object result = JSONPath.eval(array, "$.avg()");
        assertEquals(3.0, ((Number) result).doubleValue());
    }

    @Test
    public void testAvgDecimals() {
        JSONArray array = JSONArray.of(1.5, 2.5, 3.0);
        Object result = JSONPath.eval(array, "$.avg()");
        double avg = ((Number) result).doubleValue();
        assertEquals(7.0 / 3.0, avg, 0.0001);
    }

    @Test
    public void testAvgEmpty() {
        JSONArray array = new JSONArray();
        Object result = JSONPath.eval(array, "$.avg()");
        assertNull(result);
    }

    @Test
    public void testAvgNested() {
        JSONObject obj = JSONObject.of("scores", JSONArray.of(10, 20, 30));
        Object result = JSONPath.eval(obj, "$.scores.avg()");
        assertEquals(20.0, ((Number) result).doubleValue());
    }

    @Test
    public void testAvgExtract() {
        String json = "{\"values\":[10,20,30,40]}";
        Object result = JSONPath.extract(json, "$.values.avg()");
        assertEquals(25.0, ((Number) result).doubleValue());
    }

    // ========== stddev() ==========

    @Test
    public void testStddev() {
        JSONArray array = JSONArray.of(2, 4, 4, 4, 5, 5, 7, 9);
        Object result = JSONPath.eval(array, "$.stddev()");
        double stddev = ((Number) result).doubleValue();
        assertEquals(2.0, stddev, 0.0001);
    }

    @Test
    public void testStddevSingle() {
        JSONArray array = JSONArray.of(5);
        Object result = JSONPath.eval(array, "$.stddev()");
        assertEquals(0.0, ((Number) result).doubleValue());
    }

    @Test
    public void testStddevEmpty() {
        JSONArray array = new JSONArray();
        Object result = JSONPath.eval(array, "$.stddev()");
        assertNull(result);
    }

    @Test
    public void testStddevNested() {
        JSONObject obj = JSONObject.of("data", JSONArray.of(10, 10, 10));
        Object result = JSONPath.eval(obj, "$.data.stddev()");
        assertEquals(0.0, ((Number) result).doubleValue());
    }

    // ========== concat() ==========

    @Test
    public void testConcat() {
        JSONArray array = JSONArray.of("hello", " ", "world");
        Object result = JSONPath.eval(array, "$.concat()");
        assertEquals("hello world", result);
    }

    @Test
    public void testConcatMixed() {
        JSONArray array = JSONArray.of("a", 1, "b", 2);
        Object result = JSONPath.eval(array, "$.concat()");
        assertEquals("a1b2", result);
    }

    @Test
    public void testConcatNested() {
        JSONObject obj = JSONObject.of("tags", JSONArray.of("java", "json", "path"));
        Object result = JSONPath.eval(obj, "$.tags.concat()");
        assertEquals("javajsonpath", result);
    }

    @Test
    public void testConcatWithNulls() {
        JSONArray array = new JSONArray();
        array.add("a");
        array.add(null);
        array.add("b");
        Object result = JSONPath.eval(array, "$.concat()");
        assertEquals("ab", result);
    }

    @Test
    public void testConcatEmpty() {
        JSONArray array = new JSONArray();
        Object result = JSONPath.eval(array, "$.concat()");
        assertEquals("", result);
    }

    // ========== subsetof ==========

    @Test
    public void testSubsetOf() {
        String json = "[{\"tags\":[\"a\",\"b\"]},{\"tags\":[\"a\",\"x\"]},{\"tags\":[\"b\"]}]";
        Object result = JSONPath.extract(json, "$[?(@.tags subsetof ['a','b','c'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(2, arr.size());
    }

    @Test
    public void testSubsetOfAll() {
        JSONObject obj = JSONObject.of("arr", JSONArray.of("a", "b"));
        JSONArray root = JSONArray.of(obj);
        Object result = JSONPath.eval(root, "$[?(@.arr subsetof ['a','b','c'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
    }

    @Test
    public void testSubsetOfFail() {
        JSONObject obj = JSONObject.of("arr", JSONArray.of("a", "z"));
        JSONArray root = JSONArray.of(obj);
        Object result = JSONPath.eval(root, "$[?(@.arr subsetof ['a','b','c'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(0, arr.size());
    }

    @Test
    public void testSubsetOfNumbers() {
        String json = "[{\"nums\":[1,2]},{\"nums\":[1,5]}]";
        Object result = JSONPath.extract(json, "$[?(@.nums subsetof [1,2,3])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
    }

    // ========== anyof ==========

    @Test
    public void testAnyOf() {
        String json = "[{\"tags\":[\"a\",\"b\"]},{\"tags\":[\"x\",\"y\"]}]";
        Object result = JSONPath.extract(json, "$[?(@.tags anyof ['a','z'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
    }

    @Test
    public void testAnyOfNone() {
        JSONObject obj = JSONObject.of("tags", JSONArray.of("x", "y"));
        JSONArray root = JSONArray.of(obj);
        Object result = JSONPath.eval(root, "$[?(@.tags anyof ['a','b'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(0, arr.size());
    }

    @Test
    public void testAnyOfAll() {
        JSONObject obj1 = JSONObject.of("tags", JSONArray.of("a", "b"));
        JSONObject obj2 = JSONObject.of("tags", JSONArray.of("b", "c"));
        JSONArray root = JSONArray.of(obj1, obj2);
        Object result = JSONPath.eval(root, "$[?(@.tags anyof ['b'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(2, arr.size());
    }

    // ========== noneof ==========

    @Test
    public void testNoneOf() {
        String json = "[{\"tags\":[\"a\",\"b\"]},{\"tags\":[\"x\",\"y\"]}]";
        Object result = JSONPath.extract(json, "$[?(@.tags noneof ['a','b'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals(JSONArray.of("x", "y"), ((JSONObject) arr.get(0)).getJSONArray("tags"));
    }

    @Test
    public void testNoneOfAll() {
        JSONObject obj1 = JSONObject.of("tags", JSONArray.of("a"));
        JSONObject obj2 = JSONObject.of("tags", JSONArray.of("b"));
        JSONArray root = JSONArray.of(obj1, obj2);
        Object result = JSONPath.eval(root, "$[?(@.tags noneof ['x','y'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(2, arr.size());
    }

    @Test
    public void testNoneOfFail() {
        JSONObject obj = JSONObject.of("tags", JSONArray.of("a", "b"));
        JSONArray root = JSONArray.of(obj);
        Object result = JSONPath.eval(root, "$[?(@.tags noneof ['a','x'])]");
        JSONArray arr = (JSONArray) result;
        assertEquals(0, arr.size());
    }

    // ========== empty ==========

    @Test
    public void testEmptyString() {
        String json = "[{\"name\":\"\"},{\"name\":\"hello\"}]";
        Object result = JSONPath.extract(json, "$[?(@.name empty true)]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals("", ((JSONObject) arr.get(0)).getString("name"));
    }

    @Test
    public void testEmptyStringFalse() {
        String json = "[{\"name\":\"\"},{\"name\":\"hello\"}]";
        Object result = JSONPath.extract(json, "$[?(@.name empty false)]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals("hello", ((JSONObject) arr.get(0)).getString("name"));
    }

    @Test
    public void testEmptyArray() {
        String json = "[{\"arr\":[]},{\"arr\":[1,2]}]";
        Object result = JSONPath.extract(json, "$[?(@.arr empty true)]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals(0, ((JSONObject) arr.get(0)).getJSONArray("arr").size());
    }

    @Test
    public void testEmptyArrayFalse() {
        String json = "[{\"arr\":[]},{\"arr\":[1,2]}]";
        Object result = JSONPath.extract(json, "$[?(@.arr empty false)]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
        assertEquals(2, ((JSONObject) arr.get(0)).getJSONArray("arr").size());
    }

    @Test
    public void testEmptyObject() {
        JSONObject empty = new JSONObject();
        JSONObject nonEmpty = JSONObject.of("key", "value");
        JSONObject item1 = JSONObject.of("obj", empty);
        JSONObject item2 = JSONObject.of("obj", nonEmpty);
        JSONArray root = JSONArray.of(item1, item2);
        Object result = JSONPath.eval(root, "$[?(@.obj empty true)]");
        JSONArray arr = (JSONArray) result;
        assertEquals(1, arr.size());
    }

    // ========== add() ==========

    @Test
    public void testAddToArray() {
        JSONObject obj = JSONObject.of("items", JSONArray.of(1, 2, 3));
        JSONPath.add(obj, "$.items", 4);
        assertEquals(JSONArray.of(1, 2, 3, 4), obj.getJSONArray("items"));
    }

    @Test
    public void testAddToArrayString() {
        String json = "{\"items\":[1,2,3]}";
        String result = JSONPath.add(json, "$.items", 4);
        JSONObject obj = JSON.parseObject(result);
        assertEquals(4, obj.getJSONArray("items").size());
        assertEquals(4, obj.getJSONArray("items").getIntValue(3));
    }

    @Test
    public void testAddObject() {
        JSONObject obj = JSONObject.of("books", new JSONArray());
        JSONPath.add(obj, "$.books", JSONObject.of("title", "test"));
        assertEquals(1, obj.getJSONArray("books").size());
        assertEquals("test", obj.getJSONArray("books").getJSONObject(0).getString("title"));
    }

    @Test
    public void testAddNotArray() {
        JSONObject obj = JSONObject.of("name", "test");
        assertThrows(Exception.class, () -> JSONPath.add(obj, "$.name", "value"));
    }

    // ========== put() ==========

    @Test
    public void testPutNewKey() {
        JSONObject obj = JSONObject.of("data", JSONObject.of("a", 1));
        JSONPath.put(obj, "$.data", "b", 2);
        assertEquals(2, obj.getJSONObject("data").getIntValue("b"));
    }

    @Test
    public void testPutUpdateKey() {
        JSONObject obj = JSONObject.of("data", JSONObject.of("a", 1));
        JSONPath.put(obj, "$.data", "a", 99);
        assertEquals(99, obj.getJSONObject("data").getIntValue("a"));
    }

    @Test
    public void testPutToListOfMaps() {
        JSONArray books = JSONArray.of(
                JSONObject.of("title", "A"),
                JSONObject.of("title", "B")
        );
        JSONObject obj = JSONObject.of("books", books);
        JSONPath.put(obj, "$.books", "category", "fiction");
        assertEquals("fiction", books.getJSONObject(0).getString("category"));
        assertEquals("fiction", books.getJSONObject(1).getString("category"));
    }

    @Test
    public void testPutString() {
        String json = "{\"data\":{\"a\":1}}";
        String result = JSONPath.put(json, "$.data", "b", 2);
        JSONObject obj = JSON.parseObject(result);
        assertEquals(2, obj.getJSONObject("data").getIntValue("b"));
    }

    // ========== renameKey() ==========

    @Test
    public void testRenameKey() {
        JSONObject obj = JSONObject.of("data", JSONObject.of("oldName", "value"));
        JSONPath.renameKey(obj, "$.data", "oldName", "newName");
        assertNull(obj.getJSONObject("data").get("oldName"));
        assertEquals("value", obj.getJSONObject("data").getString("newName"));
    }

    @Test
    public void testRenameKeyNonExistent() {
        JSONObject obj = JSONObject.of("data", JSONObject.of("a", 1));
        JSONPath.renameKey(obj, "$.data", "nonExistent", "newName");
        assertEquals(1, obj.getJSONObject("data").getIntValue("a"));
        assertNull(obj.getJSONObject("data").get("newName"));
    }

    @Test
    public void testRenameKeyInList() {
        JSONArray books = JSONArray.of(
                JSONObject.of("category", "fiction"),
                JSONObject.of("category", "science")
        );
        JSONObject obj = JSONObject.of("books", books);
        JSONPath.renameKey(obj, "$.books", "category", "genre");
        assertEquals("fiction", books.getJSONObject(0).getString("genre"));
        assertEquals("science", books.getJSONObject(1).getString("genre"));
        assertNull(books.getJSONObject(0).get("category"));
    }

    @Test
    public void testRenameKeyString() {
        String json = "{\"store\":{\"book\":\"value\"}}";
        String result = JSONPath.renameKey(json, "$.store", "book", "books");
        JSONObject obj = JSON.parseObject(result);
        assertNull(obj.getJSONObject("store").get("book"));
        assertEquals("value", obj.getJSONObject("store").getString("books"));
    }
}
