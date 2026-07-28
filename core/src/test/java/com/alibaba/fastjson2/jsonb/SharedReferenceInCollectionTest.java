package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.alibaba.fastjson2.testutil.SharedReferenceModels.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests shared references in collections using JSONB.
 *
 * <p>Keep test cases aligned with
 * {@code com.alibaba.fastjson2.writer.SharedReferenceInCollectionTest}.</p>
 */
@Tag("jsonb")
public class SharedReferenceInCollectionTest {
    private static final JSONWriter.Feature[] WRITER_FEATURES = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    private static final JSONReader.Feature[] READER_FEATURES = {
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.IgnoreAutoTypeNotMatch,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    private static final JSONWriter.Feature[] WRITER_FEATURES_WITHOUT_REF = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    // 1. Cross-element shared values

    // 1.1 HashSet Map elements share an inlined Set
    @Test
    public void testHashSetWithSharedInnerSet() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(buildMapRows(new HashSet<>()), type);
    }

    // 1.2 LinkedHashSet Map elements share an inlined Set
    @Test
    public void testLinkedHashSetWithSharedInnerSet() {
        Type type = new TypeReference<LinkedHashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(buildMapRows(new LinkedHashSet<>()), type);
    }

    // 1.3 Two HashSet elements share an inlined Set
    @Test
    public void testHashSetWithTwoSharedElements() {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        Set<Map<String, Object>> outer = new HashSet<>();
        for (String sn : new String[]{"sn-1", "sn-2"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            outer.add(row);
        }

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(outer, type);
    }

    // 1.4 HashSet Bean elements preserve values and hash buckets
    @Test
    public void testHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<HashSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new HashSet<>()), type);
    }

    // 1.5 LinkedHashSet Bean elements preserve shared values
    @Test
    public void testLinkedHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<LinkedHashSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new LinkedHashSet<>()), type);
    }

    // 1.6 TreeSet Bean elements preserve shared values
    @Test
    public void testTreeSetWithSharedInnerSetBean() {
        Type type = new TypeReference<TreeSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new TreeSet<>()), type);
    }

    // 2. Shared fields within one element

    // 2.1 HashSet Map fields share an inlined Set
    @Test
    public void testHashSetWithSharedReferenceInsideMapElement() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapAliasesPreserved(buildAliasedMapRows(new HashSet<>(), 3), type);
    }

    // 2.2 HashSet Bean fields share an inlined Set
    @Test
    public void testHashSetWithSharedReferenceInsideBeanElement() {
        Type type = new TypeReference<HashSet<AliasedBean>>() {
        }.getType();
        assertAliasedBeanRowsPreserved(buildAliasedBeanRows(new HashSet<>()), type);
    }

    // 2.3 ReferenceDetection alone inlines shared fields
    @Test
    public void testHashSetWithoutTypeMetadataWithSharedReferenceInsideElement() {
        Set<Map<String, Object>> original = buildAliasedMapRows(new HashSet<>(), 3);
        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "same-element shared references must be inlined without type metadata"
        );

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> back =
                JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertAliasedMapRows(back, original.size());
    }

    // 2.4 A single HashSet element has shared fields
    @Test
    public void testSingleElementHashSetWithSharedReferenceInsideElement() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapAliasesPreserved(buildAliasedMapRows(new HashSet<>(), 1), type);
    }

    // 2.5 A LinkedHashSet element has shared fields
    @Test
    public void testLinkedHashSetWithSharedReferenceInsideElement() {
        Type type = new TypeReference<LinkedHashSet<Map<String, Object>>>() {
        }.getType();
        assertMapAliasesPreserved(buildAliasedMapRows(new LinkedHashSet<>(), 3), type);
    }

    // 2.6 A TreeSet Bean element has shared fields
    @Test
    public void testTreeSetWithSharedReferenceInsideBeanElement() {
        Type type = new TypeReference<TreeSet<AliasedBean>>() {
        }.getType();
        assertAliasedBeanRowsPreserved(buildAliasedBeanRows(new TreeSet<>()), type);
    }

    // 2.7 ArrayDeque preserves shared field identity
    @Test
    public void testArrayDequeWithSharedReferenceInsideElement() {
        ArrayDeque<Map<String, Object>> original = buildAliasedMapRows(new ArrayDeque<>(), 3);
        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "same-element shared references in an ArrayDeque should use a reference"
        );

        Type type = new TypeReference<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back =
                JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertAliasedMapRows(back, original.size());
        for (Map<String, Object> row : back) {
            assertSame(row.get("codes"), row.get("codesAlias"));
        }
    }

    // 2.8 LinkedHashSet preserves null and shared fields
    @Test
    public void testLinkedHashSetWithNullAndSharedReferenceInsideElement() {
        LinkedHashSet<Map<String, Object>> original = new LinkedHashSet<>();
        original.add(null);
        buildAliasedMapRows(original, 1);
        Type type = new TypeReference<LinkedHashSet<Map<String, Object>>>() {
        }.getType();

        assertMapAliasesPreserved(original, type);
    }

    // 2.9 Nested HashSet elements inline shared fields
    @Test
    public void testNestedHashSetWithSharedReferenceInsideElement() {
        MapWrapper wrapper = new MapWrapper();
        wrapper.data = buildAliasedMapRows(new HashSet<>(), 3);

        byte[] bytes = JSONB.toBytes(wrapper, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "same-element shared references in a nested Set must be inlined"
        );

        MapWrapper back = JSONB.parseObject(bytes, MapWrapper.class, READER_FEATURES);
        assertAliasedMapRows(back.data, wrapper.data.size());
    }

    // 3. Duplicate element identity

    // 3.1 ArrayList preserves duplicate element identity
    @Test
    public void testArrayListWithSameInstanceTwice() {
        Bean shared = new Bean("only", new HashSet<>(Arrays.asList("c1", "c2", "c3")));
        List<Bean> original = new ArrayList<>();
        original.add(shared);
        original.add(shared);

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "duplicate List element must be written as a reference"
        );

        Type type = new TypeReference<ArrayList<Bean>>() {
        }.getType();
        List<Bean> back = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(2, back.size());
        assertSame(back.get(0), back.get(1), "duplicate List element identity must be preserved");
    }

    // 3.2 LinkedList preserves duplicate element identity
    @Test
    public void testLinkedListWithSameInstanceTwice() {
        Bean shared = new Bean("only", new HashSet<>(Arrays.asList("c1", "c2", "c3")));
        List<Bean> original = new LinkedList<>();
        original.add(shared);
        original.add(shared);

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "duplicate LinkedList element must be written as a reference"
        );

        Type type = new TypeReference<LinkedList<Bean>>() {
        }.getType();
        List<Bean> back = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(2, back.size());
        assertSame(back.get(0), back.get(1), "duplicate LinkedList element identity must be preserved");
    }

    // 3.3 ArrayDeque preserves duplicate element identity
    @Test
    public void testArrayDequeWithSameInstanceTwice() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("sn", "only");
        shared.put("codes", new HashSet<>(Arrays.asList("c1", "c2", "c3")));
        ArrayDeque<Map<String, Object>> original = new ArrayDeque<>();
        original.add(shared);
        original.add(shared);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "duplicate element in an ordered non-List Collection should use a reference"
        );

        Type type = new TypeReference<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertEquals(2, back.size());
        assertSame(back.get(0), back.get(1));
        assertEquals(3, ((Collection<?>) back.get(0).get("codes")).size());
    }

    // 4. Shared identity in Lists

    // 4.1 ArrayList elements preserve shared inner identity
    @Test
    public void testArrayListCrossElementKeepsSharedIdentity() {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        List<Map<String, Object>> original = new ArrayList<>();
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            original.add(row);
        }

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "cross-element shared inner in a List must use a reference"
        );

        Type type = new TypeReference<ArrayList<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(3, back.size());
        Object codes0 = back.get(0).get("codes");
        assertNotNull(codes0);
        for (Map<String, Object> row : back) {
            assertSame(codes0, row.get("codes"), "List must preserve cross-element shared identity");
        }
    }

    // 4.2 LinkedList elements preserve shared inner identity
    @Test
    public void testLinkedListCrossElementKeepsSharedIdentity() {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        List<Map<String, Object>> original = new LinkedList<>();
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            original.add(row);
        }

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "cross-element shared inner in a LinkedList must use a reference"
        );

        Type type = new TypeReference<LinkedList<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(3, back.size());
        Object codes0 = back.get(0).get("codes");
        assertNotNull(codes0);
        for (Map<String, Object> row : back) {
            assertSame(codes0, row.get("codes"), "LinkedList must preserve cross-element shared identity");
        }
    }

    // 4.3 ArrayList element fields preserve shared identity
    @Test
    public void testArrayListKeepsSameElementSharedIdentity() {
        List<Map<String, Object>> original = buildAliasedMapRows(new ArrayList<>(), 2);
        Type type = new TypeReference<ArrayList<Map<String, Object>>>() {
        }.getType();

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        List<Map<String, Object>> jsonbBack = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertAliasedListRows(jsonbBack);
    }

    // 4.4 ArrayList round-trip preserves shared values
    @Test
    public void testArrayListRegressionNotAffected() {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        List<Map<String, Object>> original = new ArrayList<>();
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            original.add(row);
        }

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        Type type = new TypeReference<ArrayList<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(original.size(), back.size());
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "List round-trip must keep codes, sn=" + row.get("sn"));
            assertEquals(3, ((Collection<?>) codes).size());
        }
    }

    // 4.5 ArrayList preserves two shared identity groups
    @Test
    public void testListWithTwoDistinctSharedGroups() {
        Set<String> groupA = new HashSet<>(Arrays.asList("a1", "a2"));
        Set<String> groupB = new HashSet<>(Arrays.asList("b1", "b2", "b3"));
        List<Map<String, Object>> original = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", "sn-" + i);
            row.put("codes", i < 2 ? groupA : groupB);
            original.add(row);
        }

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        Type type = new TypeReference<ArrayList<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(4, back.size());
        assertSame(back.get(0).get("codes"), back.get(1).get("codes"));
        assertSame(back.get(2).get("codes"), back.get(3).get("codes"));
        assertEquals(2, ((Collection<?>) back.get(0).get("codes")).size());
        assertEquals(3, ((Collection<?>) back.get(2).get("codes")).size());
    }

    // 4.6 HashSet inlines two shared value groups
    @Test
    public void testHashSetWithTwoDistinctSharedGroups() {
        Set<String> groupA = new HashSet<>(Arrays.asList("a1", "a2"));
        Set<String> groupB = new HashSet<>(Arrays.asList("b1", "b2", "b3"));
        Set<Map<String, Object>> original = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", (i < 2 ? "a-" : "b-") + i);
            row.put("codes", i < 2 ? groupA : groupB);
            original.add(row);
        }

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "distinct shared groups in a Set must be inlined"
        );

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(4, back.size());
        for (Map<String, Object> row : back) {
            int expected = String.valueOf(row.get("sn")).startsWith("a") ? 2 : 3;
            assertEquals(expected, ((Collection<?>) row.get("codes")).size(), "sn=" + row.get("sn"));
        }
    }

    // 5. Shared Map values

    // 5.1 LinkedHashMap values preserve shared identity
    @Test
    public void testMapRootWithSharedSetValue() {
        Set<String> shared = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("a", shared);
        root.put("b", shared);

        byte[] bytes = JSONB.toBytes(root, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "shared Set value in a Map root must use a reference"
        );

        Type type = new TypeReference<LinkedHashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertNotNull(back.get("a"));
        assertSame(back.get("a"), back.get("b"), "Map root must preserve shared value identity");
    }

    // 5.2 HashMap values preserve shared identity
    @Test
    public void testHashMapRootWithSharedSetValue() {
        Set<String> shared = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        Map<String, Object> root = new HashMap<>();
        root.put("a", shared);
        root.put("b", shared);

        byte[] bytes = JSONB.toBytes(root, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("$ref"),
                "shared Set value in a HashMap root must use a reference"
        );

        Type type = new TypeReference<HashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertNotNull(back.get("a"));
        assertSame(back.get("a"), back.get("b"), "HashMap root must preserve shared value identity");
    }

    // 6. Nested and boundary collections

    // 6.1 Nested HashSet Bean elements preserve shared values
    @Test
    public void testNestedHashSetUnderBeanWithSharedInnerSet() {
        Wrapper wrapper = new Wrapper();
        wrapper.data = buildBeanRows(new HashSet<>());

        byte[] bytes = JSONB.toBytes(wrapper, WRITER_FEATURES);
        assertFalse(JSONB.toJSONString(bytes).contains("$ref"), "cross-element shared inner in a nested Set must be inlined");
        Wrapper back = JSONB.parseObject(bytes, Wrapper.class, READER_FEATURES);

        assertEquals(wrapper.data.size(), back.data.size());
        for (Bean bean : wrapper.data) {
            assertTrue(back.data.contains(bean), "nested set is missing bean " + bean.sn);
        }
    }

    // 6.2 Empty HashSet round-trip
    @Test
    public void testEmptyHashSetRoundTrip() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();

        byte[] bytes = JSONB.toBytes(new HashSet<>(), WRITER_FEATURES);
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertNotNull(back);
        assertTrue(back.isEmpty());
    }

    // 6.3 Single-element HashSet round-trip
    @Test
    public void testSingleElementHashSetRoundTrip() {
        Set<Map<String, Object>> outer = new HashSet<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sn", "only");
        row.put("codes", new HashSet<>(Arrays.asList("c1", "c2", "c3")));
        outer.add(row);

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(outer, type);
    }

    // 7. Cyclic references

    // 7.1 Single-element Set preserves a self-reference
    @Test
    public void testSingleElementSelfCycleRoundTrip() {
        CyclicBean bean = new CyclicBean();
        bean.name = "root";
        bean.self = bean;

        Set<CyclicBean> set = new HashSet<>();
        set.add(bean);

        assertSelfCyclesPreserved(set);
    }

    // 7.2 Single-element Set handles an indirect cycle
    @Test
    public void testSingleElementIndirectCycleDoesNotOverflow() {
        CyclicBean a = new CyclicBean();
        a.name = "a";
        CyclicBean b = new CyclicBean();
        b.name = "b";
        a.child = b;
        b.child = a;

        Set<CyclicBean> set = new HashSet<>();
        set.add(a);

        assertIndirectCyclesPreserved(set);
    }

    // 7.3 Single element preserves its Set back-reference
    @Test
    public void testSingleElementSetBackReferenceRoundTrip() {
        SetBackRefBean bean = new SetBackRefBean();
        bean.name = "root";
        Set<SetBackRefBean> set = new HashSet<>();
        bean.parentSet = set;
        set.add(bean);

        assertSetBackReferencesPreserved(set);
    }

    // 7.4 Multi-element Set handles self-references.
    // Each element writes a ".." reference to itself, so consecutive ".." paths target different
    // objects and must not be collapsed into "#-1".
    @Test
    public void testMultiElementSelfCycleDoesNotOverflow() {
        Set<CyclicBean> set = new HashSet<>();
        for (String name : new String[]{"a", "b"}) {
            CyclicBean bean = new CyclicBean();
            bean.name = name;
            bean.self = bean;
            set.add(bean);
        }

        assertSelfCyclesPreserved(set);
    }

    // 7.5 Multi-element Set handles an indirect cycle
    @Test
    public void testMultiElementIndirectCycleDoesNotOverflow() {
        CyclicBean a = new CyclicBean();
        a.name = "a";
        CyclicBean b = new CyclicBean();
        b.name = "b";
        a.child = b;
        b.child = a;

        CyclicBean marker = new CyclicBean();
        marker.name = "marker";

        Set<CyclicBean> set = new HashSet<>();
        set.add(a);
        set.add(marker);

        assertDoesNotThrow(() -> JSONB.toBytes(set, WRITER_FEATURES));
    }

    // 7.6 Elements preserve their Set back-references
    @Test
    public void testMultiElementSetBackReferenceRoundTrip() {
        Set<SetBackRefBean> set = new HashSet<>();
        for (String name : new String[]{"a", "b"}) {
            SetBackRefBean bean = new SetBackRefBean();
            bean.name = name;
            bean.parentSet = set;
            set.add(bean);
        }

        assertSetBackReferencesPreserved(set);
    }

    // 7.7 Set reuse of a stable-path cycle does not overflow
    @Test
    public void testStablePathIndirectCycleReusedInSetDoesNotOverflow() {
        CyclicBean a = new CyclicBean();
        a.name = "a";
        CyclicBean b = new CyclicBean();
        b.name = "b";
        a.child = b;
        b.child = a;

        Set<CyclicBean> set = new LinkedHashSet<>();
        set.add(b);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("stable", a);
        root.put("set", set);

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(
                root,
                JSONWriter.Feature.ReferenceDetection
        ));
        Type type = new TypeReference<LinkedHashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertStableAndSetCycles(back);
    }

    // 7.8 Restoring after the first self-reference must not make the second one inline again.
    // The object is registered at a stable path before being inlined under an unordered Set.
    @Test
    public void testSetElementWithTwoSelfReferencingCollectionFields() {
        DualCyclicBean x = new DualCyclicBean();
        x.name = "x";
        x.loop1 = new ArrayList<>();
        x.loop1.add(x);
        x.loop2 = new ArrayList<>();
        x.loop2.add(x);

        DualCyclicHolder holder = new DualCyclicHolder();
        holder.stable = new ArrayList<>();
        holder.stable.add(x);
        holder.set = new HashSet<>();
        holder.set.add(x);

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(holder, WRITER_FEATURES));
        DualCyclicHolder back = JSONB.parseObject(bytes, DualCyclicHolder.class, READER_FEATURES);

        DualCyclicBean fromSet = back.set.iterator().next();
        assertNotNull(fromSet.loop1);
        assertNotNull(fromSet.loop2);
        assertSame(fromSet, fromSet.loop1.get(0), "first self-reference must be preserved");
        assertSame(fromSet, fromSet.loop2.get(0), "second self-reference must not be inlined again");
    }

    // 7.9 The same restoration case through two indirect back-references.
    // This exercises FieldWriterObject setPath/popPath instead of the direct self-reference shortcut.
    @Test
    public void testSetElementWithTwoIndirectCycles() {
        DualLinkBean a = new DualLinkBean();
        a.name = "a";
        a.link1 = new Link();
        a.link1.back = a;
        a.link2 = new Link();
        a.link2.back = a;

        DualLinkHolder holder = new DualLinkHolder();
        holder.stable = new ArrayList<>();
        holder.stable.add(a);
        holder.set = new HashSet<>();
        holder.set.add(a);

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(holder, WRITER_FEATURES));
        DualLinkHolder back = JSONB.parseObject(bytes, DualLinkHolder.class, READER_FEATURES);

        DualLinkBean fromSet = back.set.iterator().next();
        assertNotNull(fromSet.link1);
        assertNotNull(fromSet.link2);
        assertSame(fromSet, fromSet.link1.back, "first indirect back-reference must be preserved");
        assertSame(fromSet, fromSet.link2.back, "second indirect back-reference must not be inlined again");
    }

    // 7.10 Nested Set levels each preserve parentSet back-references to their immediate enclosing Set
    @Test
    public void testNestedSetParentBackReferencesRoundTrip() {
        Set<NestedSetBackRefBean> root = new HashSet<>();
        NestedSetBackRefBean outer = new NestedSetBackRefBean();
        outer.name = "outer";
        outer.parentSet = root;
        outer.nested = new HashSet<>();
        root.add(outer);

        NestedSetBackRefBean inner = new NestedSetBackRefBean();
        inner.name = "inner";
        inner.parentSet = outer.nested;
        outer.nested.add(inner);

        Type type = new TypeReference<HashSet<NestedSetBackRefBean>>() {
        }.getType();
        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(root, WRITER_FEATURES));
        Set<NestedSetBackRefBean> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(1, back.size());
        NestedSetBackRefBean outerBack = back.iterator().next();
        assertEquals("outer", outerBack.name);
        assertSame(back, outerBack.parentSet, "outer parentSet must resolve to the root Set");
        assertNotNull(outerBack.nested);
        assertEquals(1, outerBack.nested.size());

        NestedSetBackRefBean innerBack = outerBack.nested.iterator().next();
        assertEquals("inner", innerBack.name);
        assertSame(outerBack.nested, innerBack.parentSet, "inner parentSet must resolve to its enclosing nested Set");
    }

    // 8. Enclosing and disabled references

    // 8.1 Set element inlines a shared enclosing property
    @Test
    public void testSharedReferenceFromEnclosingBeanIntoSetElement() {
        EnclosingSharedWrapper wrapper = new EnclosingSharedWrapper();
        wrapper.shared = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        wrapper.data = new HashSet<>();
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            wrapper.data.add(new Bean(sn, wrapper.shared));
        }

        byte[] bytes = JSONB.toBytes(wrapper, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "a completed enclosing property referenced by a Set element must be inlined"
        );
        EnclosingSharedWrapper jsonbBack =
                JSONB.parseObject(bytes, EnclosingSharedWrapper.class, READER_FEATURES);
        assertEnclosingSharedWrapperPreserved(wrapper, jsonbBack);
    }

    // 8.2 HashSet Map elements round-trip without references
    @Test
    public void testHashSetWithoutReferenceDetection() {
        Set<Map<String, Object>> original = buildMapRows(new HashSet<>());

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES_WITHOUT_REF);

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(original.size(), back.size());
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null, sn=" + row.get("sn"));
            assertEquals(3, ((Collection<?>) codes).size());
        }
    }

    // 8.3 HashSet Bean elements round-trip without references
    @Test
    public void testHashSetWithoutReferenceDetectionBean() {
        Set<Bean> original = buildBeanRows(new HashSet<>());

        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES_WITHOUT_REF);

        Type type = new TypeReference<HashSet<Bean>>() {
        }.getType();
        Set<Bean> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(original.size(), back.size());
        for (Bean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn);
        }
    }

    // 8.4 References after a Set scope use the stable path
    @Test
    public void testReferenceAfterCollectionScopeKeepsOriginalIdentity() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("id", 1);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("value", shared);
        Set<Map<String, Object>> data = new HashSet<>();
        data.add(row);

        Map<String, Object> original = new LinkedHashMap<>();
        original.put("shared", shared);
        original.put("data", data);
        original.put("after", shared);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("\"$ref\":\"$.shared\""),
                "reference after the collection scope must use the original stable path"
        );

        Type type = new TypeReference<LinkedHashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertSame(back.get("shared"), back.get("after"));
    }

    // 8.5 When an object first appears under an unordered Set, its first stable path becomes
    // canonical so later stable occurrences preserve identity.
    @Test
    public void testSetFirstThenStableReferencesUseStablePath() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("id", 1);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("value", shared);
        Set<Map<String, Object>> data = new HashSet<>();
        data.add(row);

        Map<String, Object> original = new LinkedHashMap<>();
        original.put("data", data);
        original.put("stable", shared);
        original.put("after", shared);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("\"$ref\":\"$.stable\""),
                "a later stable occurrence must reference the promoted stable path"
        );

        Type type = new TypeReference<LinkedHashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        assertSame(back.get("stable"), back.get("after"));
    }

    // 8.6 LinkedHashSet element paths are stable and remain referenceable from later fields.
    @Test
    public void testOrderedSetElementReferencedByLaterField() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("id", 1);

        Set<Map<String, Object>> ordered = new LinkedHashSet<>();
        ordered.add(shared);

        Map<String, Object> original = new LinkedHashMap<>();
        original.put("ordered", ordered);
        original.put("after", shared);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                JSONB.toJSONString(bytes).contains("\"$ref\":\"$.ordered[0]\""),
                "an object first seen in an ordered Set must be referenced by a later field"
        );

        Type type = new TypeReference<LinkedHashMap<String, Object>>() {
        }.getType();
        Map<String, Object> back = JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);
        List<?> orderedBack = (List<?>) back.get("ordered");
        assertSame(orderedBack.get(0), back.get("after"));
    }

    // 9. Nested Lists in non-List collections

    // 9.1 ArrayDeque preserves references in a nested List
    @Test
    public void testNestedListInArrayDequeKeepsSharedIdentity() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("id", 1);

        List<Map<String, Object>> nested = new ArrayList<>();
        nested.add(shared);
        nested.add(shared);
        ArrayDeque<List<Map<String, Object>>> original = new ArrayDeque<>();
        original.add(nested);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        Type type = new TypeReference<List<List<Map<String, Object>>>>() {
        }.getType();
        List<List<Map<String, Object>>> back =
                JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);

        List<Map<String, Object>> nestedBack = back.get(0);
        assertSame(nestedBack.get(0), nestedBack.get(1));
    }

    // 9.2 HashSet inlines references in a nested List
    @Test
    public void testNestedListInHashSetInlinesSharedReference() {
        Map<String, Object> shared = new LinkedHashMap<>();
        shared.put("id", 1);

        List<Map<String, Object>> nested = new ArrayList<>();
        nested.add(shared);
        nested.add(shared);
        HashSet<List<Map<String, Object>>> original = new HashSet<>();
        original.add(nested);

        byte[] bytes = JSONB.toBytes(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "shared references under a Set element must be inlined"
        );

        Type type = new TypeReference<List<List<Map<String, Object>>>>() {
        }.getType();
        List<List<Map<String, Object>>> back =
                JSONB.parseObject(bytes, type, JSONReader.Feature.UseNativeObject);

        List<Map<String, Object>> nestedBack = back.get(0);
        assertEquals(nestedBack.get(0), nestedBack.get(1));
    }

    private static void assertMapRowsPreserved(Set<Map<String, Object>> original, Type targetType) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "cross-element shared references in a Set must be inlined"
        );
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);

        assertEquals(original.size(), back.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null after round-trip, sn=" + row.get("sn"));
            assertEquals(expectedCodes, new HashSet<>((Collection<?>) codes), "codes changed, sn=" + row.get("sn"));
            assertTrue(back.contains(row), "Set hash bucket is broken, sn=" + row.get("sn"));
        }
    }

    private static void assertBeanRowsPreserved(Set<Bean> original, Type targetType) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "cross-element shared Bean fields in a Set must be inlined"
        );
        Set<Bean> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);

        assertEquals(original.size(), back.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (Bean bean : back) {
            assertNotNull(bean.codes, "codes should not be null after round-trip, sn=" + bean.sn);
            assertEquals(expectedCodes, bean.codes, "codes changed after round-trip, sn=" + bean.sn);
        }

        // contains detects hash bucket corruption that Set.equals can miss
        for (Bean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn + " (broken hash bucket)");
        }
    }

    private static void assertMapAliasesPreserved(
            Collection<Map<String, Object>> original,
            Type targetType
    ) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "same-element shared references in a Set must be inlined"
        );

        Collection<Map<String, Object>> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);
        assertAliasedMapRows(back, original.size());
        if (original.contains(null)) {
            assertTrue(back.contains(null), "null element should be preserved");
        }
    }

    private static void assertAliasedMapRows(
            Collection<Map<String, Object>> rows,
            int expectedSize
    ) {
        assertNotNull(rows);
        assertEquals(expectedSize, rows.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            Object codes = row.get("codes");
            Object codesAlias = row.get("codesAlias");
            assertNotNull(codes, "codes should not be null, sn=" + row.get("sn"));
            assertNotNull(codesAlias, "codesAlias should not be null, sn=" + row.get("sn"));
            assertEquals(expectedCodes, new HashSet<>((Collection<?>) codes));
            assertEquals(expectedCodes, new HashSet<>((Collection<?>) codesAlias));
            if (rows instanceof Set) {
                assertTrue(((Set<?>) rows).contains(row), "Set hash bucket is broken, sn=" + row.get("sn"));
            }
        }
    }

    private static void assertAliasedBeanRowsPreserved(
            Set<AliasedBean> original,
            Type targetType
    ) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        assertTrue(
                !JSONB.toJSONString(bytes).contains("$ref"),
                "same-element shared Bean fields in a Set must be inlined"
        );
        Set<AliasedBean> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);

        assertEquals(original.size(), back.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (AliasedBean bean : back) {
            assertNotNull(bean.codes, "codes should not be null, sn=" + bean.sn);
            assertNotNull(bean.codesAlias, "codesAlias should not be null, sn=" + bean.sn);
            assertEquals(expectedCodes, bean.codes);
            assertEquals(expectedCodes, bean.codesAlias);
        }
        for (AliasedBean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn + " (broken Set structure)");
        }
    }

    private static void assertEnclosingSharedWrapperPreserved(
            EnclosingSharedWrapper original,
            EnclosingSharedWrapper back
    ) {
        assertNotNull(back);
        assertEquals(original.shared, back.shared);
        assertEquals(original.data.size(), back.data.size());
        for (Bean bean : back.data) {
            assertNotNull(bean.codes, "codes should not be null, sn=" + bean.sn);
            assertEquals(original.shared, bean.codes, "codes changed, sn=" + bean.sn);
        }
        for (Bean bean : original.data) {
            assertTrue(back.data.contains(bean), "back is missing bean " + bean.sn + " (broken hash bucket)");
        }
    }

    private static void assertSelfCyclesPreserved(Set<CyclicBean> original) {
        Type type = new TypeReference<HashSet<CyclicBean>>() {
        }.getType();

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(original, WRITER_FEATURES));
        Set<CyclicBean> jsonbBack = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(original.size(), jsonbBack.size());
        for (CyclicBean bean : jsonbBack) {
            assertSame(bean, bean.self, "JSONB self-cycle should be preserved, name=" + bean.name);
        }
    }

    private static void assertIndirectCyclesPreserved(Set<CyclicBean> original) {
        Type type = new TypeReference<HashSet<CyclicBean>>() {
        }.getType();
        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(original, WRITER_FEATURES));
        Set<CyclicBean> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertEquals(original.size(), back.size());
        CyclicBean cycle = null;
        for (CyclicBean bean : back) {
            if ("a".equals(bean.name)) {
                cycle = bean;
                break;
            }
        }
        assertNotNull(cycle, "cycle root should be preserved");
        assertNotNull(cycle.child, "cycle child should be preserved");
        assertSame(cycle, cycle.child.child, "indirect cycle should be preserved");
    }

    private static void assertStableAndSetCycles(Map<String, Object> back) {
        Map<?, ?> stable = (Map<?, ?>) back.get("stable");
        Map<?, ?> stableChild = (Map<?, ?>) stable.get("child");
        assertSame(stable, stableChild.get("child"), "stable-path cycle should be preserved");

        Collection<?> set = (Collection<?>) back.get("set");
        assertEquals(1, set.size());
        Map<?, ?> setElement = (Map<?, ?>) set.iterator().next();
        Map<?, ?> setChild = (Map<?, ?>) setElement.get("child");
        assertSame(setElement, setChild.get("child"), "inlined Set cycle should be preserved");
    }

    private static void assertSetBackReferencesPreserved(Set<SetBackRefBean> original) {
        Type type = new TypeReference<HashSet<SetBackRefBean>>() {
        }.getType();

        byte[] bytes = assertDoesNotThrow(() -> JSONB.toBytes(original, WRITER_FEATURES));
        Set<SetBackRefBean> jsonbBack = JSONB.parseObject(bytes, type, READER_FEATURES);
        assertEquals(original.size(), jsonbBack.size());
        for (SetBackRefBean bean : jsonbBack) {
            assertSame(jsonbBack, bean.parentSet, "JSONB Set back-reference should be preserved");
        }
    }

    private static void assertAliasedListRows(List<Map<String, Object>> rows) {
        assertEquals(2, rows.size());
        for (Map<String, Object> row : rows) {
            assertNotNull(row.get("codes"));
            assertSame(row.get("codes"), row.get("codesAlias"), "List should preserve shared identity");
        }
    }
}
