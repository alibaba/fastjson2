package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        assertEquals("{\"alpha\":5,\"b\":1,\"z\":0}",
                JSON.toJSONString(new Renamed(), JSONWriter.Feature.SortFieldNamesAlphabetically));
    }

    public static class Renamed {
        public int alpha = 5;

        @JSONField(name = "z")
        public int omega() {
            return 0;
        }

        @JSONField(name = "b")
        public int beta() {
            return 1;
        }
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
        public Map<String, Integer> meta = new TreeMap<>();
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
        ObjectWriter sorted = provider.getObjectWriter(AlphabeticFalse.class, AlphabeticFalse.class, false, true);
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
}
