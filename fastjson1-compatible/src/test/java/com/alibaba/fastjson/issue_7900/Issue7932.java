package com.alibaba.fastjson.issue_7900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7932 {
    static final String OBJECT_NO_REF = "{\"listA\":[\"a\",\"b\"],\"listB\":[\"a\",\"b\"]}";
    static final String OBJECT_REF = "{\"listA\":[\"a\",\"b\"],\"listB\":{\"$ref\":\"$.listA\"}}";
    static final String ARRAY_NO_REF = "[[\"a\",\"b\"],[\"a\",\"b\"]]";
    static final String ARRAY_REF = "[[\"a\",\"b\"],{\"$ref\":\"$[0]\"}]";
    static final String OBJECT_UNQUOTED_REF = "{listA:[\"a\",\"b\"],listB:{\"$ref\":\"$.listA\"}}";
    static final String OVERRIDE_DOC = "{\"overridden\":true}";

    static class CustomObject extends JSONObject {
        CustomObject() {
            super(true);
        }
    }

    static class CustomArray extends JSONArray {
    }

    static class OverrideObject extends JSONObject {
        OverrideObject() {
            super(true);
        }

        @Override
        public String toJSONString() {
            return OVERRIDE_DOC;
        }
    }

    static List<String> sharedList() {
        return new ArrayList<String>(Arrays.asList("a", "b"));
    }

    static void putSharedList(JSONObject object) {
        List<String> shared = sharedList();
        object.put("listA", shared);
        object.put("listB", shared);
        assertSame(shared, object.get("listA"));
        assertSame(shared, object.get("listB"));
    }

    static void addSharedList(JSONArray array) {
        List<String> shared = sharedList();
        array.add(shared);
        array.add(shared);
        assertSame(shared, array.get(0));
        assertSame(shared, array.get(1));
    }

    static JSONObject objectWithSharedList() {
        JSONObject object = new JSONObject(true);
        putSharedList(object);
        return object;
    }

    static JSONArray arrayWithSharedList() {
        JSONArray array = new JSONArray();
        addSharedList(array);
        return array;
    }

    @Test
    public void disabledJSONObjectDefaults() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features | SerializerFeature.DisableCircularReferenceDetect.getMask();
            JSONObject object = objectWithSharedList();

            assertEquals(OBJECT_NO_REF, JSON.toJSONString(object), "static entry point");
            assertEquals(OBJECT_NO_REF, object.toJSONString(), "JSON#toJSONString()");
            assertEquals(OBJECT_NO_REF, object.toString(), "JSONObject#toString()");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    @Test
    public void disabledJSONArrayDefaults() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features | SerializerFeature.DisableCircularReferenceDetect.getMask();
            JSONArray array = arrayWithSharedList();

            assertEquals(ARRAY_NO_REF, JSON.toJSONString(array), "static entry point");
            assertEquals(ARRAY_NO_REF, array.toString(), "JSONArray#toString()");
            assertEquals(ARRAY_NO_REF, array.toJSONString(), "inherited JSON#toJSONString()");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    @Test
    public void defaultReferenceBehavior() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features & ~SerializerFeature.DisableCircularReferenceDetect.getMask();

            JSONObject object = objectWithSharedList();
            String objectStatic = JSON.toJSONString(object);
            assertEquals(OBJECT_REF, objectStatic, "static entry point");
            assertTrue(objectStatic.contains("$ref"));
            assertEquals(objectStatic, object.toJSONString(), "JSON#toJSONString()");
            assertEquals(objectStatic, object.toString(), "JSONObject#toString()");

            JSONArray array = arrayWithSharedList();
            String arrayStatic = JSON.toJSONString(array);
            assertEquals(ARRAY_REF, arrayStatic, "static entry point");
            assertEquals(arrayStatic, array.toString(), "JSONArray#toString()");
            assertEquals(arrayStatic, array.toJSONString(), "inherited JSON#toJSONString()");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    // unrelated-feature control: only DisableCircularReferenceDetect is honored by the instance paths
    @Test
    public void unrelatedQuoteFieldNamesUnchanged() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features & ~SerializerFeature.QuoteFieldNames.getMask();
            JSONObject object = objectWithSharedList();

            assertEquals(OBJECT_UNQUOTED_REF, JSON.toJSONString(object), "static entry point honors QuoteFieldNames");
            assertEquals(OBJECT_REF, object.toJSONString(), "JSON#toJSONString() keeps core quoting");
            assertEquals(OBJECT_REF, object.toString(), "JSONObject#toString() keeps core quoting");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    @Test
    public void customJSONObjectSubclass() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features | SerializerFeature.DisableCircularReferenceDetect.getMask();
            CustomObject object = new CustomObject();
            putSharedList(object);
            assertEquals(OBJECT_NO_REF, JSON.toJSONString(object), "static entry point");
            assertEquals(OBJECT_NO_REF, object.toJSONString(), "JSON#toJSONString()");
            assertEquals(OBJECT_NO_REF, object.toString(), "JSONObject#toString()");

            JSON.DEFAULT_GENERATE_FEATURE = features & ~SerializerFeature.DisableCircularReferenceDetect.getMask();
            CustomObject detected = new CustomObject();
            putSharedList(detected);
            assertEquals(OBJECT_REF, JSON.toJSONString(detected), "static entry point");
            assertEquals(OBJECT_REF, detected.toJSONString(), "JSON#toJSONString()");
            assertEquals(OBJECT_REF, detected.toString(), "JSONObject#toString()");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    @Test
    public void customJSONArraySubclass() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features | SerializerFeature.DisableCircularReferenceDetect.getMask();
            CustomArray array = new CustomArray();
            addSharedList(array);
            assertEquals(ARRAY_NO_REF, JSON.toJSONString(array), "static entry point");
            assertEquals(ARRAY_NO_REF, array.toString(), "JSONArray#toString()");
            assertEquals(ARRAY_NO_REF, array.toJSONString(), "inherited JSON#toJSONString()");

            JSON.DEFAULT_GENERATE_FEATURE = features & ~SerializerFeature.DisableCircularReferenceDetect.getMask();
            CustomArray detected = new CustomArray();
            addSharedList(detected);
            assertEquals(ARRAY_REF, JSON.toJSONString(detected), "static entry point");
            assertEquals(ARRAY_REF, detected.toString(), "JSONArray#toString()");
            assertEquals(ARRAY_REF, detected.toJSONString(), "inherited JSON#toJSONString()");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }

    // recursion guard: JSONAware static route keeps the override, toString() goes through super
    @Test
    public void customOverrideToJSONString() {
        int features = JSON.DEFAULT_GENERATE_FEATURE;
        try {
            JSON.DEFAULT_GENERATE_FEATURE = features | SerializerFeature.DisableCircularReferenceDetect.getMask();
            OverrideObject object = new OverrideObject();
            putSharedList(object);

            assertEquals(OVERRIDE_DOC, JSON.toJSONString(object), "static JSONAware route");
            assertEquals(OVERRIDE_DOC, object.toJSONString(), "override kept");
            assertEquals(OBJECT_NO_REF, object.toString(), "JSONObject#toString() uses super");
        } finally {
            JSON.DEFAULT_GENERATE_FEATURE = features;
        }
    }
}
