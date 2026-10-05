package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CoreReviewRegressionTest {
    @Test
    public void largeSymbolTable() {
        String[] names = new String[65537];
        for (int i = 0; i < names.length; i++) {
            names[i] = "symbol_" + i;
        }
        SymbolTable table = new SymbolTable(names);
        for (int ordinal : new int[]{1, 32768, 32769, 65536, 65537}) {
            String name = table.getName(ordinal);
            long hash = Fnv.hashCode64(name);
            assertEquals(ordinal, table.getOrdinal(name));
            assertEquals(ordinal, table.getOrdinalByHashCode(hash));
            assertEquals(name, table.getNameByHashCode(hash));
            assertEquals(hash, table.getHashCode(ordinal));
        }
        assertEquals(-1, table.getOrdinal("absent"));
    }

    @Test
    public void typedJsonbSkipsUnrelatedContainers() {
        for (Type type : new Type[]{Object.class, Integer.class, Long.class, BigDecimal.class, String.class}) {
            JSONPath path = JSONPath.of("$.value", type);
            for (Object ignored : new Object[]{JSONObject.of("nested", 7), JSONArray.of(7)}) {
                JSONObject object = JSONObject.of("ignored", ignored, "value", 42);
                try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(object))) {
                    assertEquals(path.eval(object), path.extract(reader));
                }
                object.remove("value");
                try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(object))) {
                    assertNull(path.extract(reader));
                }
            }
        }
    }

    @Test
    public void missingArrayPrefixes() {
        assertPaths(new String[]{"$.items[0]", "$.items[1]"}, "{}", null, null);
        assertPaths(new String[]{"$.items[0]", "$.items[1]"}, "{\"other\":[]}", null, null);
        for (String json : new String[]{"[]", "[[]]"}) {
            assertPaths(new String[]{"$[1][0]", "$[1][1]"}, json, null, null);
        }
    }

    @Test
    public void nestedArrayPrefixes() {
        assertPaths(new String[]{"$.a.b[0]", "$.a.b[1]"}, "{\"a\":{\"b\":[10,20]}}", 10, 20);
        assertPaths(new String[]{"$[0][0][0]", "$[0][0][1]"}, "[[[10,20]]]", 10, 20);
        assertPaths(new String[]{"$.a.b.c[0]", "$.a.b.c[1]"}, "{\"a\":{\"b\":{\"c\":[10,20]}}}", 10, 20);
        assertPaths(new String[]{"$[1].a", "$[1].b"}, "[null,{\"a\":10,\"b\":20}]", 10, 20);
        assertPaths(new String[]{"$[1].a", "$[1].b"}, "[]", null, null);
        assertPaths(new String[]{"$[1].a", "$[1].b"}, "[null]", null, null);
    }

    @Test
    public void negativeArrayIndexes() {
        assertPaths(new String[]{"$[-1][0]", "$[-1][1]"}, "[[1,2],[10,20]]", 10, 20);
        assertPaths(new String[]{"$.a.b[-2]", "$.a.b[-1]"}, "{\"a\":{\"b\":[10,20]}}", 10, 20);
    }

    @Test
    public void duplicateIndexConversions() {
        JSONPath path = JSONPath.of(new String[]{"$[0]", "$[0]"}, new Type[]{Long.class, Double.class});
        assertExtracts(path, "[1.5]", 1L, 1.5D);
    }

    @Test
    public void duplicateIndexNullOnError() {
        JSONPath path = JSONPath.of(new String[]{"$[0]", "$[0]"}, new Type[]{String.class, Integer.class},
                null, new long[]{0, JSONPath.Feature.NullOnError.mask}, null);
        assertExtracts(path, "[\"bad\"]", "bad", null);
        JSONPath firstFails = JSONPath.of(new String[]{"$[0]", "$[0]"}, new Type[]{Integer.class, String.class},
                null, new long[]{JSONPath.Feature.NullOnError.mask, 0}, null);
        assertExtracts(firstFails, "[\"bad\"]", null, "bad");
    }

    @Test
    public void scalarSkipsUnrelatedContainers() {
        JSONPath path = JSONPath.of("$.value");
        for (String json : new String[]{"{\"ignored\":{},\"value\":42}", "{\"ignored\":[],\"value\":42}"}) {
            try (JSONReader reader = JSONReader.of(json)) {
                assertEquals("42", path.extractScalar(reader));
            }
        }
    }

    @Test
    public void duplicateKeyRetainsCollection() {
        for (String expression : new String[]{"$.value", "$.nested.value", "$.nested.child.value"}) {
            JSONObject root = JSONObject.of("value", JSONArray.of(1));
            if (expression.contains(".child")) {
                root = JSONObject.of("child", root);
            }
            if (expression.contains(".nested")) {
                root = JSONObject.of("nested", root);
            }
            JSONPath path = JSONPath.of(expression);
            Object original = path.eval(root);
            path.set(root, 2, JSONReader.Feature.DuplicateKeyValueAsArray);
            path.set(root, 3, JSONReader.Feature.DuplicateKeyValueAsArray);
            assertSame(original, path.eval(root));
            assertEquals(JSONArray.of(1, 2, 3), original);
        }
    }

    @Test
    public void singleNameNullRoot() {
        JSONPath path = JSONPath.of("$.value");
        assertNull(path.eval(null));
        assertFalse(path.contains(null));
        assertEquals(new JSONArray(), JSONPath.of("$.value", JSONPath.Feature.AlwaysReturnList).eval(null));
    }

    @Test
    public void absoluteMinimumValues() {
        JSONPath path = JSONPath.of("$.abs()");
        assertEquals(2147483648L, path.eval(Integer.MIN_VALUE));
        assertEquals(new BigInteger("9223372036854775808"), path.eval(Long.MIN_VALUE));
        assertEquals(128, path.eval(Byte.MIN_VALUE));
        assertEquals(32768, path.eval(Short.MIN_VALUE));
        assertEquals(0D, path.eval(-0D));
        assertEquals(0F, path.eval(-0F));
    }

    @Test
    public void minimumIndexFormatting() {
        assertEquals("[-2147483648]", JSONPathSegmentIndex.of(Integer.MIN_VALUE).toString());
        assertEquals("$[-2147483648]", JSONPath.of("$[-2147483648].value").getParent().toString());
    }

    private static void assertPaths(String[] paths, String json, Object... expected) {
        assertExtracts(JSONPath.of(paths, new Type[]{Integer.class, Integer.class}), json, expected);
    }

    private static void assertExtracts(JSONPath path, String json, Object... expected) {
        Object object = JSON.parse(json);
        assertArrayEquals(expected, (Object[]) path.eval(object));
        assertArrayEquals(expected, (Object[]) path.extract(json));
        assertArrayEquals(expected, (Object[]) path.extract(json.getBytes(StandardCharsets.UTF_8)));
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(object))) {
            assertArrayEquals(expected, (Object[]) path.extract(reader));
        }
    }
}
