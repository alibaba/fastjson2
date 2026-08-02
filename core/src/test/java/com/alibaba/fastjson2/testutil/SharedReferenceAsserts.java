package com.alibaba.fastjson2.testutil;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alibaba.fastjson2.testutil.SharedReferenceModels.AliasedBean;
import static com.alibaba.fastjson2.testutil.SharedReferenceModels.Bean;
import static com.alibaba.fastjson2.testutil.SharedReferenceModels.CyclicBean;
import static com.alibaba.fastjson2.testutil.SharedReferenceModels.EnclosingSharedWrapper;
import static com.alibaba.fastjson2.testutil.SharedReferenceModels.SetBackRefBean;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared assertion helpers for {@code SharedReferenceInCollectionTest}
 * (JSON text and JSONB). Round-trip helpers take a {@link Codec}.
 */
public final class SharedReferenceAsserts {
    private SharedReferenceAsserts() {
    }

    /**
     * Format-specific serialize / deserialize / inspect triad.
     * {@link #write} may return {@code byte[]} (JSONB) or {@code String} (JSON text).
     */
    public interface Codec {
        Object write(Object value);

        <T> T read(Object encoded, Type type);

        String inspect(Object encoded);
    }

    public static Codec jsonb(JSONWriter.Feature[] writerFeatures, JSONReader.Feature[] readerFeatures) {
        return new Codec() {
            @Override
            public Object write(Object value) {
                return JSONB.toBytes(value, writerFeatures);
            }

            @Override
            public <T> T read(Object encoded, Type type) {
                return JSONB.parseObject((byte[]) encoded, type, readerFeatures);
            }

            @Override
            public String inspect(Object encoded) {
                return JSONB.toJSONString((byte[]) encoded);
            }
        };
    }

    public static Codec json(JSONWriter.Feature[] writerFeatures, JSONReader.Feature... readerFeatures) {
        return new Codec() {
            @Override
            public Object write(Object value) {
                return JSON.toJSONString(value, writerFeatures);
            }

            @Override
            public <T> T read(Object encoded, Type type) {
                return JSON.parseObject((String) encoded, type, readerFeatures);
            }

            @Override
            public String inspect(Object encoded) {
                return (String) encoded;
            }
        };
    }

    public static void assertMapRowsPreserved(
            Codec codec,
            Set<Map<String, Object>> original,
            Type targetType
    ) {
        Object encoded = codec.write(original);
        String text = codec.inspect(encoded);
        assertTrue(
                !text.contains("$ref"),
                "cross-element shared references in a Set must be inlined, was: " + text
        );
        Set<Map<String, Object>> back = codec.read(encoded, targetType);

        assertEquals(original.size(), back.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (Map<String, Object> row : back) {
            Object codes = row.get("codes");
            assertNotNull(codes, "codes should not be null, sn=" + row.get("sn"));
            assertEquals(expectedCodes, new HashSet<>((Collection<?>) codes), "codes changed, sn=" + row.get("sn"));
            assertTrue(back.contains(row), "Set hash bucket is broken, sn=" + row.get("sn"));
        }
    }

    public static void assertBeanRowsPreserved(Codec codec, Set<Bean> original, Type targetType) {
        Object encoded = codec.write(original);
        String text = codec.inspect(encoded);
        assertTrue(
                !text.contains("$ref"),
                "cross-element shared Bean fields in a Set must be inlined, was: " + text
        );
        Set<Bean> back = codec.read(encoded, targetType);

        assertEquals(original.size(), back.size());
        Set<String> expectedCodes = new HashSet<>(Arrays.asList("c1", "c2", "c3"));
        for (Bean bean : back) {
            assertNotNull(bean.codes, "codes should not be null, sn=" + bean.sn);
            assertEquals(expectedCodes, bean.codes, "codes changed, sn=" + bean.sn);
        }
        // contains detects hash bucket corruption that Set.equals can miss
        for (Bean bean : original) {
            assertTrue(back.contains(bean), "back is missing bean " + bean.sn + " (broken hash bucket)");
        }
    }

    public static void assertMapAliasesPreserved(
            Codec codec,
            Collection<Map<String, Object>> original,
            Type targetType
    ) {
        Object encoded = codec.write(original);
        String text = codec.inspect(encoded);
        assertTrue(
                !text.contains("$ref"),
                "same-element shared references in a Set must be inlined, was: " + text
        );

        Collection<Map<String, Object>> back = codec.read(encoded, targetType);
        assertAliasedMapRows(back, original.size());
        if (original.contains(null)) {
            assertTrue(back.contains(null), "null element should be preserved");
        }
    }

    public static void assertAliasedMapRows(
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

    public static void assertAliasedBeanRowsPreserved(
            Codec codec,
            Set<AliasedBean> original,
            Type targetType
    ) {
        Object encoded = codec.write(original);
        String text = codec.inspect(encoded);
        assertTrue(
                !text.contains("$ref"),
                "same-element shared Bean fields in a Set must be inlined, was: " + text
        );
        Set<AliasedBean> back = codec.read(encoded, targetType);

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

    public static void assertEnclosingSharedWrapperPreserved(
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

    public static void assertSelfCyclesPreserved(Codec codec, Set<CyclicBean> original) {
        Type type = new TypeReference<HashSet<CyclicBean>>() {
        }.getType();

        Object encoded = assertDoesNotThrow(() -> codec.write(original));
        Set<CyclicBean> back = codec.read(encoded, type);
        assertEquals(original.size(), back.size());
        for (CyclicBean bean : back) {
            assertSame(bean, bean.self, "self-cycle should be preserved, name=" + bean.name);
        }
    }

    public static void assertIndirectCyclesPreserved(Codec codec, Set<CyclicBean> original) {
        Type type = new TypeReference<HashSet<CyclicBean>>() {
        }.getType();
        Object encoded = assertDoesNotThrow(() -> codec.write(original));
        Set<CyclicBean> back = codec.read(encoded, type);

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

    public static void assertStableAndSetCycles(Map<String, Object> back) {
        Map<?, ?> stable = (Map<?, ?>) back.get("stable");
        Map<?, ?> stableChild = (Map<?, ?>) stable.get("child");
        assertSame(stable, stableChild.get("child"), "stable-path cycle should be preserved");

        Collection<?> set = (Collection<?>) back.get("set");
        assertEquals(1, set.size());
        Map<?, ?> setElement = (Map<?, ?>) set.iterator().next();
        Map<?, ?> setChild = (Map<?, ?>) setElement.get("child");
        assertSame(setElement, setChild.get("child"), "inlined Set cycle should be preserved");
    }

    public static void assertSetBackReferencesPreserved(Codec codec, Set<SetBackRefBean> original) {
        Type type = new TypeReference<HashSet<SetBackRefBean>>() {
        }.getType();

        Object encoded = assertDoesNotThrow(() -> codec.write(original));
        Set<SetBackRefBean> back = codec.read(encoded, type);
        assertEquals(original.size(), back.size());
        for (SetBackRefBean bean : back) {
            assertSame(back, bean.parentSet, "Set back-reference should be preserved");
        }
    }

    public static void assertAliasedListRows(List<Map<String, Object>> rows) {
        assertEquals(2, rows.size());
        for (Map<String, Object> row : rows) {
            assertNotNull(row.get("codes"));
            assertSame(row.get("codes"), row.get("codesAlias"), "List should preserve shared identity");
        }
    }
}
