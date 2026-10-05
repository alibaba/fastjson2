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
}
