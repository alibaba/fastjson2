package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip should preserve a shared inner reference held by elements of a
 * non-List {@link Collection} when
 * {@link JSONWriter.Feature#ReferenceDetection} is enabled.
 *
 * <p>
 * Two layers of the same bug:
 *
 * <ol>
 *     <li>
 *         <b>Writer-side path issue</b>: when the outer is a {@link HashSet}
 *         the shared inner object is registered at path {@code "$.<key>"}; on
 *         the read side the root is a {@code Set}, not a Bean / List, so
 *         {@code "$.<key>"} cannot be resolved and the field comes back as
 *         {@code null}. Demonstrated by the {@code Map} element tests.
 *     </li>
 *
 *     <li>
 *         <b>Hash-bucket invariant issue</b>: when the element is a Bean whose
 *         {@code hashCode} depends on the shared field, the bean is added to
 *         the outer hash container with {@code codes == null} (because the
 *         {@code $ref} resolution is delayed); after resolution the bean's
 *         {@code hashCode} changes but it is already in the wrong bucket, so
 *         {@code Set#contains} can no longer find it. Demonstrated by the
 *         {@code Bean} element tests.
 *     </li>
 * </ol>
 *
 * <p>
 * Both layers go away once the writer stops emitting {@code $ref} for
 * non-List Collection elements.
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

    @Test
    public void testHashSetWithSharedInnerSet() {
        Type type = new TypeReference<HashSet<Map<String, Object>>>() {
        }.getType();

        assertMapRowsPreserved(buildMapRows(new HashSet<Map<String, Object>>()), type);
    }

    @Test
    public void testLinkedHashSetWithSharedInnerSet() {
        Type type = new TypeReference<LinkedHashSet<Map<String, Object>>>() {
        }.getType();

        assertMapRowsPreserved(buildMapRows(new LinkedHashSet<Map<String, Object>>()), type);
    }

    @Test
    public void testHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<HashSet<Bean>>() {
        }.getType();

        assertBeanRowsPreserved(buildBeanRows(new HashSet<Bean>()), type);
    }

    @Test
    public void testLinkedHashSetWithSharedInnerSetBean() {
        Type type = new TypeReference<LinkedHashSet<Bean>>() {
        }.getType();

        assertBeanRowsPreserved(buildBeanRows(new LinkedHashSet<Bean>()), type);
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

        // Use back.contains(original-bean) on purpose: a broken back side has wrong
        // hash buckets (because beans were added with codes == null then back-filled
        // by delayed $ref resolution), so contains() lookup against back's table
        // misses elements even though they are physically present. Note Set#equals
        // is asymmetric when hash invariants are broken, so assertEquals(original,
        // back) would hide the bug.

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

    private static class Bean implements Serializable {
        private static final long serialVersionUID = 1L;

        public String sn;

        public Set<String> codes;

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
    }
}
