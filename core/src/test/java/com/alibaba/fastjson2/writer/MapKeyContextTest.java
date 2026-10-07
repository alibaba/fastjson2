package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Non-String map keys are written with the caller's context, so its date format and filters apply to them as
 * they do to values; a context with filters takes the filtering write path, which serializes such keys separately.
 */
public class MapKeyContextTest {
    public static class Dated {
        public Map<LocalDate, String> byDay = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "v"));
        public LocalDate day = LocalDate.of(2026, 10, 6);
    }

    @Test
    public void dateKeysKeepTheContextFormatWhenFiltersAreSet() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.setDateFormat("yyyy/MM/dd");
        context.configFilter((PropertyFilter) (object, name, value) -> true);
        assertEquals("{\"byDay\":{\"2026/10/06\":\"v\"},\"day\":\"2026/10/06\"}", JSON.toJSONString(new Dated(), context));
    }

    public static class Secret {
        public String password = "hunter2";
        public String user = "u";
    }

    public static class KeyedBySecret {
        public Map<Secret, String> m = new LinkedHashMap<>(Collections.singletonMap(new Secret(), "v"));
    }

    @Test
    public void beanKeysGoThroughTheContextFilters() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.configFilter((ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);
        assertEquals("{\"m\":{\"{\\\"password\\\":\\\"***\\\",\\\"user\\\":\\\"u\\\"}\":\"v\"}}",
                JSON.toJSONString(new KeyedBySecret(), context));
    }

    public static class Inner {
        public int a = 1;
    }

    @JSONType(alphabetic = false)
    public static class UnsortedKey {
        public String zeta = "z";
        public Inner first = new Inner();
        public Inner second = first;
        public String password = "hunter2";
    }

    public static class SortedKeys {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<UnsortedKey, String> m = new LinkedHashMap<>(Collections.singletonMap(new UnsortedKey(), "v"));
    }

    public static class SortedDated {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<LocalDate, String> byDay = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "v"));
    }

    @Test
    public void fieldLevelSortedKeysKeepTheContextDateFormat() {
        // no filter: the field-level sort request reaches the key through the feature word, not the context
        JSONWriter.Context context = new JSONWriter.Context();
        context.setDateFormat("yyyy/MM/dd");
        assertEquals("{\"byDay\":{\"2026/10/06\":\"v\"}}", JSON.toJSONString(new SortedDated(), context));
    }

    @Test
    public void fieldLevelSortedBeanKeysGoThroughTheContextFilters() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.configFilter((ValueFilter) (object, name, value) -> "password".equals(name) ? "***" : value);
        assertEquals("{\"m\":{\"{\\\"first\\\":{\\\"a\\\":1},\\\"password\\\":\\\"***\\\",\\\"second\\\":{\\\"a\\\":1},\\\"zeta\\\":\\\"z\\\"}\":\"v\"}}",
                JSON.toJSONString(new SortedKeys(), context));
    }

    public static class SortedListKeys {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<List<UnsortedKey>, String> m = new LinkedHashMap<>(
                Collections.singletonMap(new ArrayList<>(Collections.singletonList(new UnsortedKey())), "v"));
    }

    @Test
    public void fieldLevelSortedListKeysSortTheirBeans() {
        // the beans inside a list key take their sorted variants as in a field-level sorted list value,
        // with either creator and whether or not a filter sends the write through the context
        String expected = "{\"m\":{\"[{\\\"first\\\":{\\\"a\\\":1},\\\"password\\\":\\\"hunter2\\\",\\\"second\\\":{\\\"a\\\":1},\\\"zeta\\\":\\\"z\\\"}]\":\"v\"}}";
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            JSONWriter.Context context = new JSONWriter.Context(new ObjectWriterProvider(creator));
            assertEquals(expected, JSON.toJSONString(new SortedListKeys(), context), creator.getClass().getSimpleName());
            context.configFilter((PropertyFilter) (object, name, value) -> true);
            assertEquals(expected, JSON.toJSONString(new SortedListKeys(), context), creator.getClass().getSimpleName() + " with a filter");
        }
    }

    @Test
    public void fieldLevelSortedBeanKeysAreTheRootOfTheirReferences() {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            JSONWriter.Context context = new JSONWriter.Context(new ObjectWriterProvider(creator), JSONWriter.Feature.ReferenceDetection);
            assertEquals("{\"m\":{\"{\\\"first\\\":{\\\"a\\\":1},\\\"password\\\":\\\"hunter2\\\",\\\"second\\\":{\\\"$ref\\\":\\\"$.first\\\"},\\\"zeta\\\":\\\"z\\\"}\":\"v\"}}",
                    JSON.toJSONString(new SortedKeys(), context), creator.getClass().getSimpleName());
        }
    }

    @Test
    public void sortedBeanKeyValuesAreNotReferencedThroughTheKeyText() {
        // the text of a sorted bean key is not a path any reader resolves, so a value repeated under such keys is
        // written in full, as without the sort
        Secret shared = new Secret();
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(new Secret(), shared);
        map.put(new Secret(), shared);
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            JSONWriter.Context context = new JSONWriter.Context(new ObjectWriterProvider(creator),
                    JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.SortFieldNamesAlphabetically);
            String json = JSON.toJSONString(map, context);
            assertEquals(-1, json.indexOf("$ref"), creator.getClass().getSimpleName() + " " + json);
        }
    }

    public static class RefKeyedHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.ReferenceDetection)
        public Map<Secret, Secret> m = new java.util.HashMap<>();
    }

    @Test
    public void fieldLevelReferenceDetectionJsonBDoesNotLeakOntoTheContext() {
        // the mutating save/restore around a non-String JSONB key or skipped non-String-key value
        // must restore the context's own bit, not the merged word: otherwise a field-level bit
        // permanently turns detection on for every later document on the caller-owned context
        RefKeyedHolder holder = new RefKeyedHolder();
        holder.m.put(new Secret(), new Secret());
        holder.m.put(new Secret(), new Secret());
        JSONWriter.Context context = new JSONWriter.Context();
        com.alibaba.fastjson2.JSONB.toBytes(holder, context);
        assertEquals(0L, context.getFeatures() & JSONWriter.Feature.ReferenceDetection.mask);

        Secret shared = new Secret();
        Map<String, Object> pair = new LinkedHashMap<>();
        pair.put("first", shared);
        pair.put("second", shared);
        byte[] onUsedContext = com.alibaba.fastjson2.JSONB.toBytes(pair, context);
        byte[] onFreshContext = com.alibaba.fastjson2.JSONB.toBytes(pair, new JSONWriter.Context());
        assertEquals(java.util.Arrays.toString(onFreshContext), java.util.Arrays.toString(onUsedContext));
    }

    public static class Cyclic {
        public String name = "k";
        public Map<Object, Object> owner;
    }

    public static class CyclicHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Object, Object> m = new LinkedHashMap<>();
    }

    @Test
    public void cyclicBeanKeyTerminatesUnderSort() {
        // a sorted bean key is the root of its own document; when it reaches back into the map it
        // keys, the render must fall back to the natural spelling instead of recursing
        Map<Object, Object> map = new LinkedHashMap<>();
        Cyclic c = new Cyclic();
        c.owner = map;
        map.put(c, "v");
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> JSON.toJSONString(map,
                new JSONWriter.Context(JSONWriter.Feature.ReferenceDetection)));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> JSON.toJSONString(map,
                new JSONWriter.Context(JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.SortFieldNamesAlphabetically)));

        CyclicHolder holder = new CyclicHolder();
        Cyclic c2 = new Cyclic();
        c2.owner = holder.m;
        holder.m.put(c2, "v");
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> JSON.toJSONString(holder,
                new JSONWriter.Context(JSONWriter.Feature.ReferenceDetection)));
    }

    @Test
    public void sortedMapsKeepTheNaturalSpellingForNonBeanKeys() {
        // an ordering feature must not change how non-bean keys are spelled: BigDecimal, Boolean,
        // Double and Float keys render byte-identically with and without the sort
        Map<Object, Object> decimal = new LinkedHashMap<>();
        decimal.put(new java.math.BigDecimal("1.5"), "v");
        Map<Object, Object> bool = new LinkedHashMap<>();
        bool.put(Boolean.TRUE, "v");
        Map<Object, Object> dbl = new LinkedHashMap<>();
        dbl.put(2.5, "v");
        Map<Object, Object> flt = new LinkedHashMap<>();
        flt.put(3.5f, "v");
        for (Map<Object, Object> map : new Map[]{decimal, bool, dbl, flt}) {
            assertEquals(JSON.toJSONString(map),
                    JSON.toJSONString(map, JSONWriter.Feature.SortFieldNamesAlphabetically), JSON.toJSONString(map));
        }

        // only the bean key's field order changes: sorted, the blob is valid JSON and round-trips,
        // even when the key text contains a quote
        Map<Object, Object> bean = new LinkedHashMap<>();
        bean.put(new UnsortedKey(), "v");
        assertEquals("{\"{\\\"first\\\":{\\\"a\\\":1},\\\"password\\\":\\\"hunter2\\\",\\\"second\\\":{\\\"a\\\":1},\\\"zeta\\\":\\\"z\\\"}\":\"v\"}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
        Map<Object, Object> quoted = new LinkedHashMap<>();
        Secret key = new Secret();
        key.user = "a\"b";
        quoted.put(key, "v");
        Map reparsed = JSON.parseObject(JSON.toJSONString(quoted, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("v", reparsed.get("{\"password\":\"hunter2\",\"user\":\"a\\\"b\"}"));
    }
}
