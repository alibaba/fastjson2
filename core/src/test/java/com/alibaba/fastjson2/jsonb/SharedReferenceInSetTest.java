package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip should preserve a shared inner reference held by elements of a
 * non-List {@link Collection} when
 * {@link JSONWriter.Feature#ReferenceDetection} is enabled.
 *
 * <p>Two layers of the same bug:</p>
 * <ol>
 * <li><b>Writer-side path issue</b>: shared inner objects get {@code $ref} paths like
 * {@code "$.<key>"} that cannot be resolved when the root is a {@code Set}.
 * Map element tests catch null fields after round-trip.</li>
 * <li><b>Hash-bucket invariant issue</b>: delayed {@code $ref} resolution changes
 * {@code hashCode} after the bean is already in the wrong bucket.
 * Bean element tests use {@link Set#contains} to catch this.</li>
 * </ol>
 */
@Tag("jsonb")
public class SharedReferenceInSetTest {
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

    @Test
    public void testHashSetWithSharedInnerSet() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(buildMapRows(new HashSet<>()), type);
    }

    @Test
    public void testLinkedHashSetWithSharedInnerSet() {
        Type type = new TypeReference<LinkedHashSet<Map<String, Object>>>() {
        }.getType();
        assertMapRowsPreserved(buildMapRows(new LinkedHashSet<>()), type);
    }

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

    @Test
    public void testHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<HashSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new HashSet<>()), type);
    }

    @Test
    public void testLinkedHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<LinkedHashSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new LinkedHashSet<>()), type);
    }

    /**
     * TreeSet ({@link java.util.SortedSet}) keeps outer {@code refDetect=true} when
     * {@code size > 1}, exercising a distinct code path from {@link HashSet}.
     */
    @Test
    public void testTreeSetWithSharedInnerSetBean() {
        Type type = new TypeReference<TreeSet<Bean>>() {
        }.getType();
        assertBeanRowsPreserved(buildBeanRows(new TreeSet<>()), type);
    }

    /**
     * Single-element non-List collection with a self-referencing element must serialize
     * without {@link StackOverflowError}. Outer collection keeps reference detection when
     * {@code size == 1}; inner cycle handling must still terminate.
     */
    @Test
    public void testSingleElementSelfCycleDoesNotOverflow() {
        CyclicBean bean = new CyclicBean();
        bean.name = "root";
        bean.self = bean;

        Set<CyclicBean> set = new HashSet<>();
        set.add(bean);

        assertDoesNotThrow(() -> JSONB.toBytes(set, WRITER_FEATURES));
        assertDoesNotThrow(() -> JSON.toJSONString(set, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.FieldBased));
    }

    /**
     * Indirect (multi-hop) cycle inside a single-element non-List collection.
     */
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

        assertDoesNotThrow(() -> JSONB.toBytes(set, WRITER_FEATURES));
        assertDoesNotThrow(() -> JSON.toJSONString(set, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.FieldBased));
    }

    /**
     * Element holds a reference back to the enclosing Set (single-element case).
     */
    @Test
    public void testSingleElementSetBackReferenceDoesNotOverflow() {
        SetBackRefBean bean = new SetBackRefBean();
        bean.name = "root";
        Set<SetBackRefBean> set = new HashSet<>();
        bean.parentSet = set;
        set.add(bean);

        assertDoesNotThrow(() -> JSONB.toBytes(set, WRITER_FEATURES));
        assertDoesNotThrow(() -> JSON.toJSONString(set, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.FieldBased));
    }

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

    @Test
    public void testEmptyHashSetRoundTrip() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();

        byte[] bytes = JSONB.toBytes(new HashSet<>(), WRITER_FEATURES);
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, type, READER_FEATURES);

        assertNotNull(back);
        assertTrue(back.isEmpty());
    }

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

    @Test
    public void testNestedHashSetUnderBeanWithSharedInnerSet() {
        Wrapper wrapper = new Wrapper();
        wrapper.data = buildBeanRows(new HashSet<>());

        byte[] bytes = JSONB.toBytes(wrapper, WRITER_FEATURES);
        Wrapper back = JSONB.parseObject(bytes, Wrapper.class, READER_FEATURES);

        assertEquals(wrapper.data.size(), back.data.size());
        for (Bean bean : wrapper.data) {
            assertTrue(back.data.contains(bean), "nested set is missing bean " + bean.sn);
        }
    }

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

    /**
     * {@code write()} path: {@code JSON.toJSONString(set, ReferenceDetection)} must inline
     * shared references inside Set elements instead of emitting unresolvable {@code $ref}.
     */
    @Test
    public void testTextFormatHashSetWithSharedInnerSet() {
        Set<Map<String, Object>> original = buildMapRows(new HashSet<>());

        String str = JSON.toJSONString(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(!str.contains("$ref"), "shared reference inside Set elements must be inlined, was: " + str);

        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> back = JSON.parseObject(str, type);

        assertEquals(original.size(), back.size());
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null after text round-trip, sn=" + row.get("sn"));
            assertEquals(3, ((Collection<?>) codes).size());
        }
    }

    @Test
    public void testTextFormatHashSetWithSharedInnerSetBean() {
        Set<Bean> original = buildBeanRows(new HashSet<>());

        String str = JSON.toJSONString(original, JSONWriter.Feature.ReferenceDetection);
        assertTrue(!str.contains("$ref"), "shared reference inside Set elements must be inlined, was: " + str);

        Type type = new TypeReference<HashSet<Bean>>() {
        }.getType();
        Set<Bean> back = JSON.parseObject(str, type);

        assertEquals(original.size(), back.size());
        for (Bean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn + " after text round-trip");
        }
    }

    @Test
    public void testTextFormatWithoutReferenceDetection() {
        Set<Map<String, Object>> original = buildMapRows(new HashSet<>());

        String str = JSON.toJSONString(original);
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();
        Set<Map<String, Object>> back = JSON.parseObject(str, type);

        assertEquals(original.size(), back.size());
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null after text round-trip, sn=" + row.get("sn"));
            assertEquals(3, ((Collection<?>) codes).size());
        }
    }

    private static void assertMapRowsPreserved(Set<Map<String, Object>> original, Type targetType) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        Set<Map<String, Object>> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);

        assertEquals(original.size(), back.size());
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null after round-trip, sn=" + row.get("sn"));
            assertEquals(3, ((Collection<?>) codes).size());
        }
    }

    private static void assertBeanRowsPreserved(Set<Bean> original, Type targetType) {
        byte[] bytes = JSONB.toBytes(original, WRITER_FEATURES);
        Set<Bean> back = JSONB.parseObject(bytes, targetType, READER_FEATURES);

        // Use back.contains(original-bean): broken hash buckets hide bugs from Set#equals.
        assertEquals(original.size(), back.size());
        for (Bean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn + " (broken hash bucket)");
        }
    }

    private static <S extends Set<Map<String, Object>>> S buildMapRows(S outer) {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sn", sn);
            row.put("codes", sharedInner);
            outer.add(row);
        }
        return outer;
    }

    private static <S extends Set<Bean>> S buildBeanRows(S outer) {
        Set<String> sharedInner = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (String sn : new String[]{"sn-1", "sn-2", "sn-3"}) {
            outer.add(new Bean(sn, sharedInner));
        }
        return outer;
    }

    private static class Bean implements Serializable, Comparable<Bean> {
        private static final long serialVersionUID = 1L;

        public String sn;
        public Set<String> codes;

        public Bean() {
        }

        public Bean(String sn, Set<String> codes) {
            this.sn = sn;
            this.codes = codes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Bean)) {
                return false;
            }
            Bean bean = (Bean) o;
            return Objects.equals(sn, bean.sn)
                    && Objects.equals(codes, bean.codes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, codes);
        }

        @Override
        public int compareTo(Bean o) {
            return sn.compareTo(o.sn);
        }
    }

    /** Default {@code Object} identity so instances can sit in a hash Set while holding cycles. */
    public static class CyclicBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public CyclicBean self;
        public CyclicBean child;
    }

    /** Element references the enclosing Set (default {@code Object} identity). */
    public static class SetBackRefBean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public Set<SetBackRefBean> parentSet;
    }

    static class Wrapper implements Serializable {
        private static final long serialVersionUID = 1L;

        public Set<Bean> data;
    }
}
