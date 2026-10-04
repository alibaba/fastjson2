package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
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
}
