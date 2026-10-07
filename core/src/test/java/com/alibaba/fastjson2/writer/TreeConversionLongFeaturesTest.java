package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeConversionLongFeaturesTest {
    @JSONType(alphabetic = false)
    public static class SortChild {
        public int zebra = 3;
        public int apple = 1;
    }

    @Test
    public void longOverloadCarriesFeatures() {
        // non-adapter writers (lists, maps) in the tree-conversion helper must see the caller word
        Object converted = ObjectWriterAdapter.toJSON(Collections.singletonList(new SortChild()),
                JSONWriter.Feature.SortFieldNamesAlphabetically.mask);
        assertEquals("[{\"apple\":1,\"zebra\":3}]", converted.toString());
    }

    public static class BeanWithNullField {
        public String label;
        public int rank = 1;
    }

    @Test
    public void mergedContextFeaturesHonored() {
        // global defaults merge with the caller word, as on JSON.toJSON(Object, Feature...)
        JSON.config(JSONWriter.Feature.WriteNulls, true);
        try {
            JSONObject tree = (JSONObject) ObjectWriterAdapter.toJSON(new BeanWithNullField(), 0L);
            assertTrue(tree.containsKey("label"), tree.toString());
        } finally {
            JSON.config(JSONWriter.Feature.WriteNulls, false);
        }
    }

    public static class Node {
        public String name;
        public Node peer;
    }

    @Test
    public void referenceDetectionUsesMergedContextFeatures() {
        // with global reference detection, a cycle must take the reference-aware fallback path;
        // conversion through toJSONObject would recurse forever on the two-node cycle
        JSON.config(JSONWriter.Feature.ReferenceDetection, true);
        try {
            Node a = new Node();
            a.name = "a";
            Node b = new Node();
            b.name = "b";
            a.peer = b;
            b.peer = a;
            Object converted = ObjectWriterAdapter.toJSON(a, 0L);
            assertTrue(converted.toString().contains("$ref"), converted.toString());
        } finally {
            JSON.config(JSONWriter.Feature.ReferenceDetection, false);
        }
    }

    public static class ItemsOuter {
        public java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
    }

    @Test
    public void sortMapEntriesByKeysReachesNestedTreeMaps() {
        // the canonical combination the SortFieldNamesAlphabetically javadoc pairs: the map-sort
        // bit must reach the maps inside the tree, as it does through toJSONString
        java.util.Map<String, Object> zetaFirst = new java.util.LinkedHashMap<>();
        zetaFirst.put("zeta", 1);
        zetaFirst.put("alpha", 2);
        ItemsOuter outer = new ItemsOuter();
        outer.items.add(zetaFirst);
        Object converted = ObjectWriterAdapter.toJSON(outer, JSONWriter.Feature.SortMapEntriesByKeys.mask);
        assertEquals("{\"items\":[{\"alpha\":2,\"zeta\":1}]}", converted.toString());
    }

    public static class NestedNullsInner {
        public String label;
        public int rank = 1;
    }

    public static class NestedNullsOuter {
        public NestedNullsInner inner = new NestedNullsInner();
    }

    public static class NestedFieldBasedInner {
        private int hidden = 5;
    }

    public static class NestedFieldBasedOuter {
        public NestedFieldBasedInner inner = new NestedFieldBasedInner();
    }

    public enum Rank {
        ACE,
        KING;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static class NestedEnumOuter {
        public NestedEnumOuter.Inner inner = new NestedEnumOuter.Inner();

        public static class Inner {
            public Rank rank = Rank.KING;
        }
    }

    @Test
    public void treeFeaturesMembersApplyAtDepthTwo() {
        // each TREE_FEATURES member must reach the nested bean's conversion, not just the root
        JSONObject nulls = (JSONObject) ObjectWriterAdapter.toJSON(new NestedNullsOuter(),
                JSONWriter.Feature.WriteNulls.mask);
        assertTrue(((JSONObject) nulls.get("inner")).containsKey("label"), nulls.toString());

        JSONObject fieldBased = (JSONObject) ObjectWriterAdapter.toJSON(new NestedFieldBasedOuter(),
                JSONWriter.Feature.FieldBased.mask);
        assertEquals(5, ((JSONObject) fieldBased.get("inner")).get("hidden"));

        JSONObject enumDefault = (JSONObject) ObjectWriterAdapter.toJSON(new NestedEnumOuter(), 0L);
        assertEquals(Rank.KING, ((JSONObject) enumDefault.get("inner")).get("rank"));
        JSONObject enumNamed = (JSONObject) ObjectWriterAdapter.toJSON(new NestedEnumOuter(),
                JSONWriter.Feature.WriteEnumsUsingName.mask);
        assertEquals("KING", ((JSONObject) enumNamed.get("inner")).get("rank"));
    }

    @com.alibaba.fastjson2.annotation.JSONType(serializeFeatures = JSONWriter.Feature.WriteClassName)
    public static class TypeNamed {
        public String name = "t";
    }

    @Test
    public void writeClassNameTypeSkipsTheDirectTreeBranch() {
        // the WriteClassName guard of the toJSON fast path: a type-level WriteClassName must take
        // the round-trip fallback so the @type entry survives
        JSONObject tree = com.alibaba.fastjson2.JSONObject.from(new TypeNamed());
        assertTrue(tree.containsKey("@type"), tree.toString());
    }
}
