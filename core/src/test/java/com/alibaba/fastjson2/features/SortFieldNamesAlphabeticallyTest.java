package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("features")
public class SortFieldNamesAlphabeticallyTest {
    public static class Bean {
        public int zebra = 3;
        public int mango = 1;
        public int apple = 2;
    }

    @Test
    public void defaultIsAlreadyAlphabetic() {
        // fastjson2 sorts bean properties by default (fastjson2.writer.alphabetic=true),
        // so the feature is a no-op for ordinary beans under the default provider
        Bean bean = new Bean();
        assertEquals("{\"apple\":2,\"mango\":1,\"zebra\":3}", JSON.toJSONString(bean));
        assertEquals("{\"apple\":2,\"mango\":1,\"zebra\":3}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @Test
    public void sortByWireName() {
        // the sort key is the WIRE name: member names order alpha, beta, omega, zebraWire
        // ({alpha,b,z,a} on the wire) while the wire names order a, alpha, b, z
        assertEquals("{\"a\":4,\"alpha\":5,\"b\":1,\"z\":0}",
                JSON.toJSONString(new Renamed(), JSONWriter.Feature.SortFieldNamesAlphabetically));
        // unsorted output keeps declaration order, so the discriminating orders stay observable
        assertEquals("{\"z\":0,\"alpha\":5,\"b\":1,\"a\":4}", JSON.toJSONString(new Renamed()));
    }

    @JSONType(alphabetic = false)
    public static class Renamed {
        @JSONField(name = "z")
        public int omega;
        public int alpha = 5;
        @JSONField(name = "b")
        public int beta = 1;
        @JSONField(name = "a")
        public int zebraWire = 4;
    }

    @JSONType(alphabetic = false)
    public static class AlphabeticFalse {
        public int zebra = 3;
        public int apple = 1;
    }

    @Test
    public void featureOverridesAnnotationAlphabeticFalse() {
        // the bean opted out of alphabetic ordering; the per-call feature sorts it anyway,
        // which is what canonical-JSON (digest/signature) flows need
        AlphabeticFalse bean = new AlphabeticFalse();
        assertEquals("{\"zebra\":3,\"apple\":1}", JSON.toJSONString(bean));
        assertEquals("{\"apple\":1,\"zebra\":3}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @JSONType(alphabetic = false)
    public static class Outer {
        public int zulu = 9;
        public AlphabeticFalse inner = new AlphabeticFalse();
        public List<AlphabeticFalse> items = new ArrayList<>();

        public Outer() {
            items.add(new AlphabeticFalse());
        }
    }

    @Test
    public void nestedBeansAndItemListsSortedRecursively() {
        Outer outer = new Outer();
        assertEquals("{\"zulu\":9,\"inner\":{\"zebra\":3,\"apple\":1},\"items\":[{\"zebra\":3,\"apple\":1}]}",
                JSON.toJSONString(outer));
        assertEquals("{\"inner\":{\"apple\":1,\"zebra\":3},\"items\":[{\"apple\":1,\"zebra\":3}],\"zulu\":9}",
                JSON.toJSONString(outer, JSONWriter.Feature.SortFieldNamesAlphabetically));
        // natural ordering must be intact again afterwards (no cross-context writer reuse)
        assertEquals("{\"zulu\":9,\"inner\":{\"zebra\":3,\"apple\":1},\"items\":[{\"zebra\":3,\"apple\":1}]}",
                JSON.toJSONString(outer));
    }

    @Test
    public void sortingStartsFromSortedContext() {
        // reverse order: sorted variant first, then natural; isolation must hold in both directions
        Outer outer = new Outer();
        assertEquals("{\"inner\":{\"apple\":1,\"zebra\":3},\"items\":[{\"apple\":1,\"zebra\":3}],\"zulu\":9}",
                JSON.toJSONString(outer, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"zulu\":9,\"inner\":{\"zebra\":3,\"apple\":1},\"items\":[{\"zebra\":3,\"apple\":1}]}",
                JSON.toJSONString(outer));
    }

    @Test
    public void pojoTreeConversionSortsNestedWriters() {
        // JSONObject.from must forward the feature word so nested writers sort too
        Outer outer = new Outer();
        com.alibaba.fastjson2.JSONObject tree = com.alibaba.fastjson2.JSONObject.from(outer,
                JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertEquals("{\"inner\":{\"apple\":1,\"zebra\":3},\"items\":[{\"apple\":1,\"zebra\":3}],\"zulu\":9}",
                tree.toString());
    }

    @JSONType(alphabetic = false)
    public static class UnwrappedInner {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class UnwrappedHolder {
        public int aaa;

        @JSONField(unwrapped = true)
        public UnwrappedInner inner = new UnwrappedInner();
    }

    @Test
    public void unwrappedTreeMatchesStringOutput() {
        UnwrappedHolder holder = new UnwrappedHolder();
        String viaString = JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertEquals("{\"aaa\":0,\"apple\":1,\"zebra\":3}", viaString);
        assertEquals(viaString,
                com.alibaba.fastjson2.JSONObject.from(holder, JSONWriter.Feature.SortFieldNamesAlphabetically).toString());
    }

    public static class SortedFieldHolder {
        public int aaa;

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public AlphabeticFalse inner = new AlphabeticFalse();
    }

    @Test
    public void fieldAnnotatedTreeMatchesStringOutput() {
        SortedFieldHolder holder = new SortedFieldHolder();
        String viaString = JSON.toJSONString(holder);
        assertEquals("{\"aaa\":0,\"inner\":{\"apple\":1,\"zebra\":3}}", viaString);
        assertEquals(viaString, com.alibaba.fastjson2.JSONObject.from(holder).toString());
    }

    @JSONType(alphabetic = false)
    public static class SortedBase {
        public int zebra = 3;
    }

    public static class SortedSub
            extends SortedBase {
        public int apple = 1;
    }

    public static class SortedHolderSuper {
        public SortedBase data = new SortedSub();
    }

    @Test
    public void treeConversionReResolvesByRuntimeTypeUnderSortedVariant() {
        // the sorted variant never primes initValueClass, so conversion must re-resolve the
        // value writer by runtime class instead of writing the declared base type's fields only
        SortedHolderSuper holder = new SortedHolderSuper();
        JSON.toJSONString(holder); // warm the natural path first, as in production
        assertEquals("{\"data\":{\"apple\":1,\"zebra\":3}}",
                com.alibaba.fastjson2.JSONObject.from(holder, JSONWriter.Feature.SortFieldNamesAlphabetically).toString());
    }

    @JSONType(alphabetic = false)
    public static class Shape {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class EveryShapeHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Shape scalar = new Shape();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<Shape> list = new ArrayList<>();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public java.util.Set<Shape> set = new java.util.LinkedHashSet<>();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Shape[] finalArr = {new Shape()};

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Object[] openArr = {new Shape()};

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<String, Shape> rows = new LinkedHashMap<>();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Shape, String> byKey = new LinkedHashMap<>();

        public Shape plain = new Shape();

        public EveryShapeHolder() {
            list.add(new Shape());
            set.add(new Shape());
            rows.put("k", new Shape());
            byKey.put(new Shape(), "v");
        }
    }

    @Test
    public void fieldAnnotationSortsEveryFieldShape() {
        EveryShapeHolder holder = new EveryShapeHolder();
        String json = JSON.toJSONString(holder);
        assertFieldShapes(json, "JSON");
        String jsonb = JSON.toJSONString(
                com.alibaba.fastjson2.JSONB.parseObject(com.alibaba.fastjson2.JSONB.toBytes(holder)));
        assertFieldShapes(jsonb, "JSONB");
    }

    private static void assertFieldShapes(String json, String label) {
        String sortedItem = "{\"apple\":1,\"zebra\":3}";
        assertTrue(json.contains("\"scalar\":" + sortedItem), label + " scalar: " + json);
        assertTrue(json.contains("\"list\":[" + sortedItem + "]"), label + " list: " + json);
        assertTrue(json.contains("\"set\":[" + sortedItem + "]"), label + " set: " + json);
        assertTrue(json.contains("\"finalArr\":[" + sortedItem + "]"), label + " finalArr: " + json);
        assertTrue(json.contains("\"openArr\":[" + sortedItem + "]"), label + " openArr: " + json);
        assertTrue(json.contains("\"rows\":{\"k\":" + sortedItem + "}"), label + " rows: " + json);

        int byKeyAt = json.indexOf("\"byKey\"");
        assertTrue(byKeyAt >= 0, label + " byKey missing: " + json);
        int byKeyEnd = json.indexOf("},\"finalArr\"", byKeyAt);
        assertTrue(byKeyEnd > byKeyAt, label + " byKey segment unterminated: " + json);
        String segment = json.substring(byKeyAt, byKeyEnd);
        if (segment.contains("{\\")) {
            // blob-rendering arms: the sorted evidence must be inside the key blob itself,
            // never satisfied from unrelated sibling fields
            int appleAt = segment.indexOf("apple\\\":1");
            int zebraAt = segment.indexOf("zebra\\\":3");
            assertTrue(appleAt >= 0 && zebraAt > appleAt,
                    label + " byKey item not sorted in the key blob: " + segment);
        } else {
            // identity-toString key shapes have no field content inside the key text
            String tail = json.substring(byKeyAt);
            int appleAt = tail.indexOf("\"apple\":1");
            int zebraAt = tail.indexOf("\"zebra\":3");
            assertTrue(appleAt >= 0 && zebraAt > appleAt,
                    label + " byKey item not sorted: " + tail);
        }

        // unannotated sibling keeps declaration order
        assertTrue(json.contains("\"plain\":{\"zebra\":3,\"apple\":1}"), label + " plain: " + json);
    }

    public static class NullableItem {
        public String name;
        public int rank = 1;
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNulls)
    public static class NullsOwner {
        public String label;
        public List<NullableItem> items = java.util.Collections.singletonList(new NullableItem());
    }

    @Test
    public void treeConversionKeepsTypeFeaturesOnTheirType() {
        // the owner's type-level WriteNulls applies to the owner's fields, not to its items
        NullsOwner owner = new NullsOwner();
        assertEquals("{\"items\":[{\"rank\":1}],\"label\":null}", JSON.toJSONString(owner));
        assertEquals("{\"items\":[{\"rank\":1}],\"label\":null}",
                JSON.toJSONString(com.alibaba.fastjson2.JSONObject.from(owner), JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void customProviderWithAlphabeticOff() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.setAlphabetic(false);

        Bean bean = new Bean();
        String natural = JSON.toJSONString(bean, new JSONWriter.Context(provider));
        assertEquals("{\"zebra\":3,\"mango\":1,\"apple\":2}", natural);

        // per-call feature restores sorted output even though the provider default is unsorted
        assertEquals("{\"apple\":2,\"mango\":1,\"zebra\":3}",
                JSON.toJSONString(bean,
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));

        // and natural stays natural afterwards
        assertEquals(natural, JSON.toJSONString(bean, new JSONWriter.Context(provider)));
    }

    @Test
    public void mapEntriesUnaffected() {
        Map<String, Integer> map = new TreeMap<>();
        map.put("b", 1);
        map.put("a", 2);
        // bean sorting must not reorder maps; SortMapEntriesByKeys covers that
        assertEquals("{\"a\":2,\"b\":1}", JSON.toJSONString(map, JSONWriter.Feature.SortFieldNamesAlphabetically));
        LinkedHashMap<String, Integer> inserted = new LinkedHashMap<>();
        inserted.put("b", 1);
        inserted.put("a", 2);
        assertEquals("{\"b\":1,\"a\":2}", JSON.toJSONString(inserted, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"a\":2,\"b\":1}", JSON.toJSONString(inserted,
                JSONWriter.Feature.SortFieldNamesAlphabetically,
                JSONWriter.Feature.SortMapEntriesByKeys));
    }

    @Test
    public void canonicalCombination() {
        CanonicalBean bean = new CanonicalBean();
        assertEquals("{\"id\":7,\"meta\":{\"a\":2,\"b\":1}}",
                JSON.toJSONString(bean,
                        JSONWriter.Feature.SortFieldNamesAlphabetically,
                        JSONWriter.Feature.SortMapEntriesByKeys));
    }

    public static class CanonicalBean {
        public Map<String, Integer> meta = new java.util.LinkedHashMap<>();
        public long id = 7;

        public CanonicalBean() {
            meta.put("b", 1);
            meta.put("a", 2);
        }
    }

    @Test
    public void explicitOrdinalTakesPrecedence() {
        assertEquals("{\"b\":1,\"a\":2}",
                JSON.toJSONString(new Ordered(), JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    public static class Ordered {
        @JSONField(name = "b", ordinal = 0)
        public int beta = 1;

        @JSONField(name = "a", ordinal = 1)
        public int alpha = 2;
    }

    @Test
    public void reflectiveCreator() {
        ObjectWriterCreator creator = new ObjectWriterCreator();
        ObjectWriter objectWriter = creator.createObjectWriter(
                AlphabeticFalse.class,
                JSONWriter.Feature.SortFieldNamesAlphabetically.mask,
                JSONFactory.getDefaultObjectWriterProvider()
        );
        List<String> names = new ArrayList<>();
        for (Object w : objectWriter.getFieldWriters()) {
            names.add(((FieldWriter) w).fieldName);
        }
        assertEquals("[apple, zebra]", names.toString());
    }

    @Test
    public void sortedWritersCachedSeparately() {
        JSON.toJSONString(new AlphabeticFalse());
        JSON.toJSONString(new AlphabeticFalse(), JSONWriter.Feature.SortFieldNamesAlphabetically);
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter natural = provider.getObjectWriter(AlphabeticFalse.class, AlphabeticFalse.class, false);
        ObjectWriter sorted = provider.getObjectWriter(AlphabeticFalse.class, AlphabeticFalse.class,
                JSONWriter.Feature.SortFieldNamesAlphabetically.mask);
        assertNotEquals(natural, sorted);
    }

    public static class WithArray {
        public AlphabeticFalse[] inners = new AlphabeticFalse[]{new AlphabeticFalse()};
    }

    @Test
    public void beanArrayFieldSorted() {
        WithArray bean = new WithArray();
        assertEquals("{\"inners\":[{\"zebra\":3,\"apple\":1}]}", JSON.toJSONString(bean));
        assertEquals("{\"inners\":[{\"apple\":1,\"zebra\":3}]}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @JSONType(alphabetic = false)
    public static class WithMap {
        public Map<String, AlphabeticFalse> entries = new LinkedHashMap<>();
        public int id = 9;

        public WithMap() {
            entries.put("k", new AlphabeticFalse());
        }
    }

    @Test
    public void typedMapOfBeansSorted() {
        WithMap bean = new WithMap();
        assertEquals("{\"entries\":{\"k\":{\"zebra\":3,\"apple\":1}},\"id\":9}", JSON.toJSONString(bean));
        assertEquals("{\"entries\":{\"k\":{\"apple\":1,\"zebra\":3}},\"id\":9}",
                JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"entries\":{\"k\":{\"zebra\":3,\"apple\":1}},\"id\":9}", JSON.toJSONString(bean));
    }

    @Test
    public void mapFieldKeepsTypedResolutionUnderWriteClassName() {
        // the map field keeps declared key/value types on the sorted path too, so values
        // sort without gaining a per-entry @type (the untyped provider writer would add one),
        // also after the shared provider cell for the runtime Map type is already warm.
        // (the reflective creator renders class names under WriteClassName unconditionally,
        // so the no-spurious-type assertion is creator-conditional, like the sibling test)
        boolean asm = new ObjectWriterProvider().getCreator() instanceof ObjectWriterCreatorASM;
        WithMap bean = new WithMap();
        JSON.toJSONString(bean); // warm the shared provider cell for the runtime Map type
        String sorted = JSON.toJSONString(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.SortFieldNamesAlphabetically);
        assertTrue(sorted.contains("\"apple\":1,\"zebra\":3"), sorted);
        if (asm) {
            assertFalse(sorted.contains(
                    "\"@type\":\"com.alibaba.fastjson2.features.SortFieldNamesAlphabeticallyTest$AlphabeticFalse\""),
                    sorted);
        }
    }

    @JSONType(alphabetic = false, serializeFeatures = JSONWriter.Feature.BeanToArray)
    public static class ArrayForm {
        public int zulu = 8;
        public int apple = 6;
    }

    @Test
    public void beanToArrayOrderKeptPositional() {
        // BeanToArray output order is positional; the sort feature must not reorder elements
        ArrayForm bean = new ArrayForm();
        assertEquals("[8,6]", JSON.toJSONString(bean));
        assertEquals("[8,6]", JSON.toJSONString(bean, JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    @JSONType(alphabetic = false)
    public static class Item {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class Container {
        @JSONField(contentAs = Item.class)
        public List<Object> items = new ArrayList<>();
    }

    public static class FieldSortItem {
        @JSONField(contentAs = Item.class, serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<Object> contentAsList = new ArrayList<>();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Item single = new Item();

        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<Item> typed = new ArrayList<>();

        public FieldSortItem() {
            contentAsList.add(new Item());
            typed.add(new Item());
        }
    }

    @Test
    public void fieldLevelSortReachesContentAsResolution() {
        // identical field-level annotation on three shapes: the contentAs arm must sort too;
        // a fresh provider keeps the outcome independent of other tests' global registrations
        String json = JSON.toJSONString(new FieldSortItem(), new JSONWriter.Context(new ObjectWriterProvider()));
        assertEquals(
                "{\"contentAsList\":[{\"apple\":1,\"zebra\":3}],\"single\":{\"apple\":1,\"zebra\":3},\"typed\":[{\"apple\":1,\"zebra\":3}]}",
                json);
    }

    @Test
    public void contentAsItemWriterNeverSharedAcrossVariants() {
        // a writer installed via register() must serve both variants without the
        // contentAs-hoisted item writer leaking sorted output into the natural variant
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        provider.registerIfAbsent(Item.class,
                new ObjectWriterCreator().createObjectWriter(Item.class, 0L, provider));

        String naturalExpected = "{\"items\":[{\"zebra\":3,\"apple\":1}]}";
        String sortedExpected = "{\"items\":[{\"apple\":1,\"zebra\":3}]}";

        // first order: natural first, then sorted, then natural again
        Container holder = new Container();
        holder.items.add(new Item());
        assertEquals(naturalExpected, JSON.toJSONString(holder));
        assertEquals(sortedExpected, JSON.toJSONString(holder, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals(naturalExpected, JSON.toJSONString(holder));

        // second order: sorted first, then natural
        Container holder2 = new Container();
        holder2.items.add(new Item());
        assertEquals(sortedExpected, JSON.toJSONString(holder2, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals(naturalExpected, JSON.toJSONString(holder2));
    }

    public static class NestedHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Object value;
    }

    static List<Object> listOf(Object item) {
        return new ArrayList<>(Collections.singletonList(item));
    }

    @Test
    public void fieldLevelSortReachesBeansInNestedContainersWithEitherCreator() {
        // the sort word travels through nested lists, arrays, Optional and AtomicReference as the context sort does;
        // the reflective field writer merges it into the context, the ASM writer passes it as the feature word
        String sorted = "{\"apple\":1,\"zebra\":3}";
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            Object[] values = {
                    listOf(listOf(new Item())),
                    new Object[]{listOf(new Item())},
                    Optional.of(listOf(new Item())),
                    new AtomicReference<>(new Item())
            };
            String[] expected = {
                    "{\"value\":[[" + sorted + "]]}",
                    "{\"value\":[[" + sorted + "]]}",
                    "{\"value\":[" + sorted + "]}",
                    "{\"value\":" + sorted + "}"
            };
            for (int i = 0; i < values.length; i++) {
                NestedHolder holder = new NestedHolder();
                holder.value = values[i];
                String message = creator.getClass().getSimpleName() + " " + values[i].getClass().getSimpleName();
                assertEquals(expected[i], JSON.toJSONString(holder, new JSONWriter.Context(new ObjectWriterProvider(creator))), message);
                byte[] jsonb = JSONB.toBytes(holder, new JSONWriter.Context(new ObjectWriterProvider(creator)));
                assertEquals(expected[i], JSON.toJSONString(JSONB.parse(jsonb)), message + " JSONB");
            }
        }
    }
}
