package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("features")
public class ErrorOnDuplicateKeysTest {
    @Test
    public void parseObject() {
        String str = "{\"item\":1,\"item\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void parseObjectDuplicateNullValue() {
        // the first occurrence has a null value; put-based detection would miss it
        String str = "{\"item\":null,\"item\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void parseObjectUniqueKeys() {
        String str = "{\"a\":1,\"b\":null,\"c\":[1,2],\"d\":{\"e\":3}}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys);
        assertEquals(1, object.getIntValue("a"));
        assertTrue(object.containsKey("b"));
        assertEquals(JSONArray.of(1, 2), object.getJSONArray("c"));
        assertEquals(3, object.getJSONObject("d").getIntValue("e"));
    }

    @Test
    public void duplicateWithReferenceValue() {
        // a duplicate key whose second occurrence is a $ref value must still be rejected
        String str = "{\"a\":1,\"a\":{\"$ref\":\"$.b\"},\"b\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void duplicateWithIgnoreNullNullFirst() {
        String str = "{\"a\":null,\"a\":1}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str,
                        JSONReader.Feature.ErrorOnDuplicateKeys,
                        JSONReader.Feature.IgnoreNullPropertyValue));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void duplicateWithIgnoreNullNullSecond() {
        String str = "{\"a\":1,\"a\":null}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str,
                        JSONReader.Feature.ErrorOnDuplicateKeys,
                        JSONReader.Feature.IgnoreNullPropertyValue));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void typedMapWithIgnoreNull() {
        String str = "{\"a\":null,\"a\":1}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, new TypeReference<Map<String, Integer>>() {
                }, JSONReader.Feature.ErrorOnDuplicateKeys, JSONReader.Feature.IgnoreNullPropertyValue));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void stringMapWithIgnoreNull() {
        String str = "{\"a\":null,\"a\":\"x\"}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, new TypeReference<Map<String, String>>() {
                }, JSONReader.Feature.ErrorOnDuplicateKeys, JSONReader.Feature.IgnoreNullPropertyValue));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void ignoreNullStillHonoredWithoutStrictFeature() {
        String str = "{\"a\":null,\"a\":1}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.IgnoreNullPropertyValue);
        assertEquals(1, object.getIntValue("a"));
        assertEquals(1, object.size());
    }

    @Test
    public void parseObjectNestedDuplicate() {
        String str = "{\"a\":{\"x\":1,\"x\":2}}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : x"));
    }

    @Test
    public void parseAny() {
        String str = "{\"item\":1,\"item\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parse(str, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void precedenceOverDuplicateKeyValueAsArray() {
        String str = "{\"item\":1,\"item\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str,
                        JSONReader.Feature.ErrorOnDuplicateKeys,
                        JSONReader.Feature.DuplicateKeyValueAsArray));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void typedMap() {
        String str = "{\"item\":1,\"item\":2}";
        TypeReference<Map<String, Integer>> typeReference = new TypeReference<Map<String, Integer>>() {
        };
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, typeReference, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void typedMapString() {
        String str = "{\"item\":\"a\",\"item\":\"b\"}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, new TypeReference<Map<String, String>>() {
                }, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void typedMapUniqueKeys() {
        String str = "{\"a\":1,\"b\":2}";
        Map<String, Integer> map = JSON.parseObject(str, new TypeReference<Map<String, Integer>>() {
        }, JSONReader.Feature.ErrorOnDuplicateKeys);
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }

    @Test
    public void readerOf() {
        String str = "{\"item\":1,\"item\":2}";
        try (JSONReader reader = JSONReader.of(str)) {
            reader.getContext().config(JSONReader.Feature.ErrorOnDuplicateKeys);
            Map<String, Object> map = new LinkedHashMap<>();
            JSONException error = assertThrows(JSONException.class, () -> reader.read(map, 0));
            assertTrue(error.getMessage().contains("duplicate key : item"));
        }
    }

    @Test
    public void hashMap() {
        String str = "{\"item\":1,\"item\":2}";
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, HashMap.class, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : item"));
    }

    @Test
    public void disabledByDefault() {
        String str = "{\"item\":1,\"item\":2}";
        JSONObject object = JSON.parseObject(str);
        assertEquals(2, object.getIntValue("item"));
    }

    @Test
    public void multiValueBranchArraySecond() {
        // first occurrence scalar, duplicate occurrence array: the multiValue branch must reject
        String str = "{\"a\":\"x\",\"a\":[\"y\"]}";
        TypeReference<Map<String, java.util.List<String>>> typeReference =
                new TypeReference<Map<String, java.util.List<String>>>() {
                };
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, typeReference, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void multiValueBranchScalarSecond() {
        String str = "{\"a\":[\"x\"],\"a\":\"y\"}";
        TypeReference<Map<String, java.util.List<String>>> typeReference =
                new TypeReference<Map<String, java.util.List<String>>>() {
                };
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(str, typeReference, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key : a"));
    }

    @Test
    public void mixedFormKeyDuplicateNormalized() {
        // one JSON key spelled as unquoted int and quoted string: detected as duplicates by the
        // normalized seen-set, with the stored key left untouched
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject("{1:\"a\",\"1\":\"b\"}", JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(error.getMessage().contains("duplicate key"));

        assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"1\":\"b\",1:\"a\"}", JSONReader.Feature.ErrorOnDuplicateKeys));
        JSONObject ok = JSON.parseObject("{1:\"a\",\"2\":\"b\"}", JSONReader.Feature.ErrorOnDuplicateKeys);
        assertEquals("a", ok.get(1));
        assertEquals("b", ok.get("2"));
    }

    @Test
    public void preSeededDestinationDoesNotFalsePositive() {
        JSONObject target = new JSONObject();
        target.put("timeout", 30);
        readInto(target, "{\"timeout\":60}", JSONReader.Feature.ErrorOnDuplicateKeys);
        // the strict feature must not be tripped by content already in the destination map
        assertEquals(60, target.get("timeout"));

        assertThrows(JSONException.class,
                () -> readInto(target, "{\"timeout\":60,\"timeout\":61}",
                        JSONReader.Feature.ErrorOnDuplicateKeys));
    }

    private static void readInto(Map<String, Object> target, String json, JSONReader.Feature... features) {
        try (JSONReader reader = JSONReader.of(json)) {
            reader.getContext().config(features);
            reader.read(target, JSONReader.Feature.of(features));
        }
    }

    @Test
    public void pooledObjectSupplierResult() {
        JSONObject pooled = new JSONObject();
        pooled.put("a", 1);
        java.util.function.Supplier<Map> supplier = () -> pooled;
        JSONFactory.setDefaultObjectSupplier(supplier);
        try {
            JSONObject first = JSON.parseObject("{\"a\":2}", JSONReader.Feature.ErrorOnDuplicateKeys);
            assertEquals(2, first.get("a"));
            JSONObject second = JSON.parseObject("{\"a\":4}", JSONReader.Feature.ErrorOnDuplicateKeys);
            assertEquals(4, second.get("a"));
        } finally {
            JSONFactory.setDefaultObjectSupplier(null);
        }
    }

    @Test
    public void perObjectScopesAreIndependent() {
        String str = "{\"a\":{\"x\":1},\"b\":{\"x\":2}}";
        JSONObject object = JSON.parseObject(str, JSONReader.Feature.ErrorOnDuplicateKeys);
        assertEquals(1, object.getJSONObject("a").getIntValue("x"));

        String nested = "{\"l\":[{\"x\":1},{\"x\":2}]}";
        JSONObject list = JSON.parseObject(nested, JSONReader.Feature.ErrorOnDuplicateKeys);
        assertEquals(1, list.getJSONArray("l").getJSONObject(0).getIntValue("x"));
    }

    public static class StrictPayloadHolder {
        @com.alibaba.fastjson2.annotation.JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object> data;
    }

    @Test
    public void annotationDrivenDepthOneAndTwo() {
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"data\":{\"b\":1,\"b\":2}}", StrictPayloadHolder.class));
        assertDuplicateKeyMessage(error, "b");

        JSONException nested = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"data\":{\"a\":{\"b\":1,\"b\":2}}}", StrictPayloadHolder.class));
        assertDuplicateKeyMessage(nested, "b");

        String ok = JSON.toJSONString(
                JSON.parseObject("{\"data\":{\"a\":{\"b\":1}}}", StrictPayloadHolder.class));
        assertEquals("{\"data\":{\"a\":{\"b\":1}}}", ok);
    }

    private static void assertDuplicateKeyMessage(Throwable error, String key) {
        Throwable leaf = error;
        while (leaf.getCause() != null && leaf.getCause() != leaf) {
            leaf = leaf.getCause();
        }
        String message = error.getMessage() + " <- " + (leaf == error ? "" : leaf.getMessage());
        assertTrue(leaf.getMessage().contains("duplicate key : " + key), message);
    }

    @Test
    public void annotationDrivenStrictnessCrossesArrays() {
        // field-level strictness must reach objects nested inside arrays too
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"data\":{\"l\":[{\"x\":1,\"x\":2}]}}", StrictPayloadHolder.class));
        assertDuplicateKeyMessage(error, "x");
    }

    @Test
    public void readObjectWordCrossesArrays() {
        try (JSONReader jsonReader = JSONReader.of("{\"l\":[{\"x\":1,\"x\":2}]}")) {
            JSONException error = assertThrows(JSONException.class,
                    () -> jsonReader.readObject(JSONReader.Feature.ErrorOnDuplicateKeys.mask));
            assertTrue(error.getMessage().contains("duplicate key : x"));
        }
    }

    @Test
    public void typedReadWordCrossesArrays() {
        try (JSONReader jsonReader = JSONReader.of("{\"a\":{\"x\":1,\"x\":2}}")) {
            Map<String, Object> map = new LinkedHashMap<>();
            JSONException error = assertThrows(JSONException.class,
                    () -> jsonReader.read(map, String.class, Object.class, JSONReader.Feature.ErrorOnDuplicateKeys.mask));
            assertTrue(error.getMessage().contains("duplicate key : x"));
        }
    }

    @Test
    public void readObjectPerCallFeatures() {
        // the word alone (no context config) must reject duplicates, including through arrays
        String str = "{\"a\":{\"l\":[{\"b\":1,\"b\":2}]}}";
        java.io.StringReader reader = new java.io.StringReader(str);
        try (com.alibaba.fastjson2.JSONReader jsonReader = com.alibaba.fastjson2.JSONReader.of(reader)) {
            java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
            JSONException error = assertThrows(JSONException.class,
                    () -> jsonReader.read(map, JSONReader.Feature.ErrorOnDuplicateKeys.mask));
            assertTrue(error.getMessage().contains("duplicate key : b"));
        }
    }

    public static class ObjBox {
        public Object data;
    }

    @Test
    public void duplicatedTypeDiscriminatorRejected() {
        // the discriminator is a key too; two of them must be rejected, not silently last-win
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(
                        "{\"data\":{\"@type\":\"a.B\",\"@type\":\"c.D\"}}",
                        ObjBox.class,
                        JSONReader.Feature.ErrorOnDuplicateKeys));
        assertDuplicateKeyMessage(error, "@type");

        // root level: the registration precedes the @type handling
        JSONException root = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"@type\":\"x.Y\",\"@type\":\"other\"}",
                        JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(root.getMessage().contains("duplicate key"));

        // a single discriminator is not a duplicate
        ObjBox ok = JSON.parseObject(
                "{\"data\":{\"@type\":\"a.B\",\"n\":1}}",
                ObjBox.class,
                JSONReader.Feature.ErrorOnDuplicateKeys);
        Map<?, ?> data = (Map<?, ?>) ok.data;
        assertEquals("a.B", data.get("@type"));
        assertEquals(1, data.get("n"));
    }

    @Test
    public void duplicatedTypeDiscriminatorRejectedTypedMap() {
        JSONException error = assertThrows(JSONException.class,
                () -> JSON.parseObject(
                        "{\"@type\":\"java.util.HashMap\",\"@type\":\"java.util.HashMap\"}",
                        new TypeReference<Map<Object, Object>>() {
                        },
                        JSONReader.Feature.ErrorOnDuplicateKeys,
                        JSONReader.Feature.SupportAutoType));
        assertTrue(error.getMessage().contains("duplicate key"));

        Map<Object, Object> ok = JSON.parseObject(
                "{\"@type\":\"java.util.HashMap\",\"n\":1}",
                new TypeReference<Map<Object, Object>>() {
                },
                JSONReader.Feature.ErrorOnDuplicateKeys,
                JSONReader.Feature.SupportAutoType);
        assertEquals(1, ok.get("n"));
    }

    @Test
    public void readObjectWithFeaturesOnJSONBReader() {
        byte[] bytes = com.alibaba.fastjson2.JSONB.toBytes(JSONObject.of("a", 1, "b", JSONObject.of("c", 2)));
        Map<String, Object> expected = JSONReader.ofJSONB(bytes).readObject();
        assertEquals(expected, JSONReader.ofJSONB(bytes).readObject(0L));
        assertEquals(expected, JSONReader.ofJSONB(bytes).readObject(JSONReader.Feature.ErrorOnDuplicateKeys.mask));
    }

    public static class StrictTypedListHolder {
        @com.alibaba.fastjson2.annotation.JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public java.util.List<Map<String, Object>> rows;

        @com.alibaba.fastjson2.annotation.JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object>[] cells;
    }

    @Test
    public void typedContainerFieldsEnforceStrictness() {
        // the strict bit travels to item readers of typed list/array fields, not just maps
        JSONException list = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"rows\":[{\"b\":1,\"b\":2}]}", StrictTypedListHolder.class));
        assertDuplicateKeyMessage(list, "b");
        JSONException array = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"cells\":[{\"b\":1,\"b\":2}]}", StrictTypedListHolder.class));
        assertDuplicateKeyMessage(array, "b");
    }

    public static class AnyCollect {
        public Map<String, Object> extras = new LinkedHashMap<>();

        @com.alibaba.fastjson2.annotation.JSONField(unwrapped = true)
        public void add(String name, Object value) {
            extras.put(name, value);
        }
    }

    @Test
    public void anySetterValuesEnforceStrictness() {
        String json = "{\"l\":[{\"x\":1,\"x\":2}]}";
        JSONException contextLevel = assertThrows(JSONException.class,
                () -> JSON.parseObject(json, AnyCollect.class, JSONReader.Feature.ErrorOnDuplicateKeys));
        assertDuplicateKeyMessage(contextLevel, "x");
    }

    @Test
    public void objectAndArrayMapKeysGetTheStrictWord() {
        // structural JSON map-keys also get the per-call strict word; without it the duplicate
        // inside a key was silently last-win (the parser drops one member of the key object)
        JSONException objectKey = assertThrows(JSONException.class,
                () -> JSON.parse("{{\"a\":1,\"a\":2}:\"v\"}",
                        JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(objectKey.getMessage().contains("duplicate key : a"), objectKey.getMessage());
        JSONException arrayKey = assertThrows(JSONException.class,
                () -> JSON.parse("{[{\"x\":1,\"x\":2}]:1}",
                        JSONReader.Feature.ErrorOnDuplicateKeys));
        assertTrue(arrayKey.getMessage().contains("duplicate key : x"), arrayKey.getMessage());
    }

    @Test
    public void typedMapObjectKeysGetTheStrictWord() {
        try (JSONReader jsonReader = JSONReader.of("{{\"a\":1,\"a\":2}:\"v\"}")) {
            JSONException error = assertThrows(JSONException.class,
                    () -> jsonReader.readObject(JSONReader.Feature.ErrorOnDuplicateKeys.mask));
            assertTrue(error.getMessage().contains("duplicate key"));
        }
    }
}
