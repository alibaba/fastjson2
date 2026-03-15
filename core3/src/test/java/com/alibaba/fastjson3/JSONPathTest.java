package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTest {
    static final String STORE_JSON = """
            {
              "store": {
                "book": [
                  {"category":"reference","author":"Nigel Rees","title":"Sayings","price":8.95},
                  {"category":"fiction","author":"Evelyn Waugh","title":"Sword","price":12.99},
                  {"category":"fiction","author":"Herman Melville","title":"Moby Dick","price":8.99},
                  {"category":"fiction","author":"Tolkien","title":"Lord of the Rings","price":22.99}
                ],
                "bicycle": {"color":"red","price":19.95}
              }
            }
            """;

    // ==================== Root ====================

    @Test
    public void testRoot() {
        JSONPath path = JSONPath.of("$");
        Object root = JSON.parse("{\"a\":1}");
        assertSame(root, path.eval(root));
        assertTrue(path.isDefinite());
    }

    // ==================== Single name ====================

    @Test
    public void testSingleName() {
        JSONPath path = JSONPath.of("$.store");
        Object root = JSON.parse(STORE_JSON);
        Object store = path.eval(root);
        assertInstanceOf(JSONObject.class, store);
        assertTrue(path.isDefinite());
    }

    @Test
    public void testNestedName() {
        String title = JSONPath.eval(STORE_JSON, "$.store.bicycle.color", String.class);
        assertEquals("red", title);
    }

    // ==================== Array index ====================

    @Test
    public void testArrayIndex() {
        String author = JSONPath.eval(STORE_JSON, "$.store.book[0].author", String.class);
        assertEquals("Nigel Rees", author);
    }

    @Test
    public void testNegativeIndex() {
        String author = JSONPath.eval(STORE_JSON, "$.store.book[-1].author", String.class);
        assertEquals("Tolkien", author);
    }

    // ==================== Wildcard ====================

    @Test
    @SuppressWarnings("unchecked")
    public void testWildcard() {
        JSONPath path = JSONPath.of("$.store.book[*].author");
        assertFalse(path.isDefinite());

        Object root = JSON.parse(STORE_JSON);
        List<String> authors = (List<String>) path.eval(root);
        assertEquals(4, authors.size());
        assertEquals("Nigel Rees", authors.get(0));
        assertEquals("Tolkien", authors.get(3));
    }

    // ==================== Array slice ====================

    @Test
    @SuppressWarnings("unchecked")
    public void testSlice() {
        JSONPath path = JSONPath.of("$.store.book[0:2].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(2, titles.size());
        assertEquals("Sayings", titles.get(0));
        assertEquals("Sword", titles.get(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSliceWithStep() {
        JSONPath path = JSONPath.of("$.store.book[0:4:2].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(2, titles.size());
        assertEquals("Sayings", titles.get(0));
        assertEquals("Moby Dick", titles.get(1));
    }

    // ==================== Recursive descent ====================

    @Test
    @SuppressWarnings("unchecked")
    public void testRecursiveDescent() {
        JSONPath path = JSONPath.of("$..author");
        Object root = JSON.parse(STORE_JSON);
        List<String> authors = (List<String>) path.eval(root);
        assertEquals(4, authors.size());
        assertFalse(path.isDefinite());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRecursiveDescentPrice() {
        JSONPath path = JSONPath.of("$..price");
        Object root = JSON.parse(STORE_JSON);
        List<Object> prices = (List<Object>) path.eval(root);
        assertEquals(5, prices.size()); // 4 books + 1 bicycle
    }

    // ==================== Filter expressions ====================

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterLessThan() {
        JSONPath path = JSONPath.of("$.store.book[?@.price < 10].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(2, titles.size());
        assertTrue(titles.contains("Sayings"));
        assertTrue(titles.contains("Moby Dick"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterEquals() {
        JSONPath path = JSONPath.of("$.store.book[?@.category == 'fiction'].author");
        Object root = JSON.parse(STORE_JSON);
        List<String> authors = (List<String>) path.eval(root);
        assertEquals(3, authors.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterAnd() {
        JSONPath path = JSONPath.of("$.store.book[?@.price < 10 && @.category == 'fiction'].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(1, titles.size());
        assertEquals("Moby Dick", titles.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilterOr() {
        JSONPath path = JSONPath.of("$.store.book[?@.price > 20 || @.price < 9].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(3, titles.size());
    }

    // ==================== Bracket notation ====================

    @Test
    public void testBracketName() {
        String color = JSONPath.eval(STORE_JSON, "$['store']['bicycle']['color']", String.class);
        assertEquals("red", color);
    }

    // ==================== Multi-index ====================

    @Test
    @SuppressWarnings("unchecked")
    public void testMultiIndex() {
        JSONPath path = JSONPath.of("$.store.book[0,2].title");
        Object root = JSON.parse(STORE_JSON);
        List<String> titles = (List<String>) path.eval(root);
        assertEquals(2, titles.size());
        assertEquals("Sayings", titles.get(0));
        assertEquals("Moby Dick", titles.get(1));
    }

    // ==================== Type conversion ====================

    @Test
    public void testTypeConversion() {
        Double price = JSONPath.eval(STORE_JSON, "$.store.book[0].price", Double.class);
        assertEquals(8.95, price, 0.001);
    }

    @Test
    public void testIntConversion() {
        String json = "{\"count\": 42}";
        int count = JSONPath.of("$.count").eval(JSON.parse(json), int.class);
        assertEquals(42, count);
    }

    // ==================== Convenience API ====================

    @Test
    public void testJsonEval() {
        String author = JSON.eval(STORE_JSON, "$.store.book[0].author", String.class);
        assertEquals("Nigel Rees", author);
    }

    @Test
    public void testExtract() {
        JSONPath path = JSONPath.of("$.store.bicycle.price");
        Double price = path.extract(STORE_JSON, Double.class);
        assertEquals(19.95, price, 0.001);
    }

    // ==================== Edge cases ====================

    @Test
    public void testMissingProperty() {
        JSONPath path = JSONPath.of("$.nonexistent");
        Object root = JSON.parse("{\"a\":1}");
        assertNull(path.eval(root));
    }

    @Test
    public void testNullRoot() {
        JSONPath path = JSONPath.of("$.a");
        assertNull(path.eval(null));
    }

    @Test
    public void testEmptyArray() {
        JSONPath path = JSONPath.of("$[0]");
        Object root = JSON.parse("[]");
        assertNull(path.eval(root));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWildcardOnObject() {
        JSONPath path = JSONPath.of("$.store.bicycle.*");
        Object root = JSON.parse(STORE_JSON);
        List<Object> values = (List<Object>) path.eval(root);
        assertEquals(2, values.size()); // color + price
    }

    // ==================== Stream Mode ====================

    @Test
    public void testStreamExtractSingleName() {
        JSONPath path = JSONPath.of("$.store");
        try (JSONParser parser = JSONParser.of(STORE_JSON)) {
            Object result = path.extract(parser);
            assertNotNull(result);
            assertInstanceOf(JSONObject.class, result);
        }
    }

    @Test
    public void testStreamExtractNestedName() {
        JSONPath path = JSONPath.of("$.store.bicycle.color");
        try (JSONParser parser = JSONParser.of(STORE_JSON)) {
            Object result = path.extract(parser);
            assertEquals("red", result);
        }
    }

    @Test
    public void testStreamExtractArrayIndex() {
        JSONPath path = JSONPath.of("$.store.book[0].author");
        try (JSONParser parser = JSONParser.of(STORE_JSON)) {
            Object result = path.extract(parser);
            assertEquals("Nigel Rees", result);
        }
    }

    @Test
    public void testStreamExtractFromBytes() {
        JSONPath path = JSONPath.of("$.store.bicycle.price");
        byte[] bytes = STORE_JSON.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Double price = path.extract(bytes, Double.class);
        assertEquals(19.95, price, 0.001);
    }

    @Test
    public void testStreamFallbackForComplexPath() {
        // Wildcard is not streamable — should fall back to Tree Mode
        JSONPath path = JSONPath.of("$.store.book[*].author");
        try (JSONParser parser = JSONParser.of(STORE_JSON)) {
            Object result = path.extract(parser);
            assertInstanceOf(List.class, result);
            assertEquals(4, ((List<?>) result).size());
        }
    }

    // ==================== set/remove ====================

    @Test
    public void testSetSingleName() {
        JSONObject obj = JSON.object("name", "Alice");
        JSONPath.of("$.name").set(obj, "Bob");
        assertEquals("Bob", obj.getString("name"));
    }

    @Test
    public void testSetNestedPath() {
        Object root = JSON.parse("{\"store\":{\"bicycle\":{\"color\":\"red\"}}}");
        JSONPath.of("$.store.bicycle.color").set(root, "blue");
        assertEquals("blue", JSONPath.eval(root, "$.store.bicycle.color", String.class));
    }

    @Test
    public void testSetArrayIndex() {
        Object root = JSON.parse("{\"items\":[1,2,3]}");
        JSONPath.of("$.items[0]").set(root, 99);
        assertEquals(99, JSONPath.eval(root, "$.items[0]", Integer.class));
    }

    @Test
    public void testRemoveName() {
        JSONObject obj = JSON.object("name", "Alice").fluentPut("age", 30);
        boolean removed = JSONPath.of("$.name").remove(obj);
        assertTrue(removed);
        assertNull(obj.get("name"));
        assertEquals(30, obj.getInteger("age"));
    }

    @Test
    public void testRemoveArrayIndex() {
        Object root = JSON.parse("{\"items\":[1,2,3]}");
        boolean removed = JSONPath.of("$.items[1]").remove(root);
        assertTrue(removed);
        assertEquals(2, ((List<?>) JSONPath.eval(root, "$.items", Object.class)).size());
    }
}
