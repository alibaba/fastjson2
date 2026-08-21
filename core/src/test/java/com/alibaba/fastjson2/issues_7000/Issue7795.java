package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Collections#emptySet()}/{@link Collections#emptyList()}/{@link Collections#emptyMap()} are
 * JVM-wide singletons, so {@link com.alibaba.fastjson2.writer.ObjectWriterProvider#isNotReferenceDetect(Class)}
 * excludes them from reference tracking. But the ASM-generated bean field writers
 * (see {@code ObjectWriterCreatorASM}) call the raw {@code JSONWriter.setPath0}/{@code popPath0}
 * directly for non-{@code Object}-declared fields, bypassing that exclusion on the push side, while
 * {@code popPath0} only special-cased the two singletons by identity on the pop side. That push/pop
 * asymmetry left {@code JSONWriter.path} one level too deep after writing a shared empty collection,
 * corrupting every path computed afterwards and producing a bogus {@code $ref} for the next occurrence.
 */
public class Issue7795 {
    public static class Model {
        private Set<Integer> ids;
        private Map<String, Integer> counts;

        public Model() {
        }

        public Model(Set<Integer> ids) {
            this.ids = ids;
        }

        public Model(Map<String, Integer> counts) {
            this.counts = counts;
        }

        public Set<Integer> getIds() {
            return ids;
        }

        public void setIds(Set<Integer> ids) {
            this.ids = ids;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }

        public void setCounts(Map<String, Integer> counts) {
            this.counts = counts;
        }
    }

    @Test
    public void testSharedEmptySetAcrossMapEntries() {
        Set<Integer> sharedEmptySet = Collections.emptySet();

        Map<Integer, Model> sourceModels = new LinkedHashMap<>();
        sourceModels.put(1001, new Model(sharedEmptySet));
        sourceModels.put(1002, new Model(sharedEmptySet));

        String json = JSON.toJSONString(sourceModels, JSONWriter.Feature.ReferenceDetection);
        assertFalse(json.contains("$ref"), "shared empty collections must not be turned into $ref: " + json);

        Map<Integer, Model> parsed = JSON.parseObject(json, new TypeReference<Map<Integer, Model>>() {
        });
        assertEquals(Collections.emptySet(), parsed.get(1001).getIds());
        assertEquals(Collections.emptySet(), parsed.get(1002).getIds());
    }

    @Test
    public void testSharedEmptyMapAcrossMapEntries() {
        Map<String, Integer> sharedEmptyMap = Collections.emptyMap();

        Map<Integer, Model> sourceModels = new LinkedHashMap<>();
        sourceModels.put(1001, new Model(sharedEmptyMap));
        sourceModels.put(1002, new Model(sharedEmptyMap));

        String json = JSON.toJSONString(sourceModels, JSONWriter.Feature.ReferenceDetection);
        assertFalse(json.contains("$ref"), "shared empty collections must not be turned into $ref: " + json);

        Map<Integer, Model> parsed = JSON.parseObject(json, new TypeReference<Map<Integer, Model>>() {
        });
        assertEquals(Collections.emptyMap(), parsed.get(1001).getCounts());
        assertEquals(Collections.emptyMap(), parsed.get(1002).getCounts());
    }

    /**
     * {@code ObjectWriterImplList.writeJSONB} is the only other caller of the raw
     * {@code setPath0(int, Object)} overload, and its own {@code refDetect} pre-check already
     * excludes singleton classes before ever reaching it - so no current caller can trip this
     * overload unguarded. It is fixed anyway for defense-in-depth, since it is a {@code public}
     * method on {@code JSONWriter} with the same "raw path-tracking" contract as the
     * {@code FieldWriter} overload that caused this issue. Exercise that contract directly.
     */
    @Test
    public void testSetPath0IntOverloadExcludesSingletons() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.ReferenceDetection);
        jsonWriter.setRootObject(new Object());

        Set<Integer> sharedEmptySet = Collections.emptySet();

        assertNull(jsonWriter.setPath0(0, sharedEmptySet));
        jsonWriter.popPath0(sharedEmptySet);

        assertNull(jsonWriter.setPath0(1, sharedEmptySet),
                "a JVM-wide singleton must never be resolved to a $ref, even via the raw overload");
        jsonWriter.popPath0(sharedEmptySet);
    }
}
