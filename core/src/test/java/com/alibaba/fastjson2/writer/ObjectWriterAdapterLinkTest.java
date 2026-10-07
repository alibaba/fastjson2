package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectWriterAdapterLinkTest {
    // resolves entries for both storage shapes (weak references and direct entries),
    // so the same checks also witness the old strong-reference implementation
    private static List<ObjectWriterAdapter> liveVariants(Object[] entries) {
        List<ObjectWriterAdapter> live = new ArrayList<>();
        for (Object entry : entries) {
            Object resolved = entry instanceof WeakReference ? ((WeakReference<?>) entry).get() : entry;
            if (resolved != null) {
                live.add((ObjectWriterAdapter) resolved);
            }
        }
        return live;
    }

    public static class RentCreds {
        public String password = "hunter2";
        public String name = "alice";
    }

    @Test
    public void linkedVariantsReleasedAfterUnregister() {
        ObjectWriter writer = ObjectWriters.objectWriter(RentCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (RentCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (RentCreds s) -> s.name));
        ObjectWriterAdapter source = (ObjectWriterAdapter) writer;

        ObjectWriterProvider provider = new ObjectWriterProvider();
        for (int i = 0; i < 50; i++) {
            provider.register(RentCreds.class, writer);
            provider.unregister(RentCreds.class);
        }
        // advisory collection: retry within a deadline instead of trusting one call to be
        // synchronous, so the outcome tracks the code under test, not the collector's mood
        int live = liveVariants(source.linkedVariants).size();
        long deadline = System.currentTimeMillis() + 5000;
        while (live > 0 && System.currentTimeMillis() < deadline) {
            System.gc();
            System.runFinalization();
            Thread.yield();
            live = liveVariants(source.linkedVariants).size();
        }
        assertEquals(0, live, "unregistered variants must become collectible, not stay attached");

        // the next link compacts the cleared entries instead of growing the array further,
        // and the published array is dense: interior null slots would NPE later setters and links
        provider.register(RentCreds.class, writer);
        assertTrue(source.linkedVariants.length <= 4,
                "expected compaction, array length " + source.linkedVariants.length);
        for (Object entry : (Object[]) (Object) source.linkedVariants) {
            assertTrue(entry != null, "linkedVariants must not publish null slots");
            if (entry instanceof java.lang.ref.WeakReference) {
                assertTrue(((java.lang.ref.WeakReference<?>) entry).get() != null,
                        "linkedVariants must not publish cleared entries while the provider holds the variant");
            }
        }
    }

    public static class LinkTargets {
        public String beta = "b";
        public String alpha = "a";
    }

    @Test
    public void linkingIsIdempotentAndAppendsDistinctVariants() {
        ObjectWriterAdapter source = (ObjectWriterAdapter) ObjectWriters.objectWriter(LinkTargets.class,
                ObjectWriters.fieldWriter("beta", String.class, (LinkTargets t) -> t.beta),
                ObjectWriters.fieldWriter("alpha", String.class, (LinkTargets t) -> t.alpha));

        ObjectWriterProvider p1 = new ObjectWriterProvider();
        p1.register(LinkTargets.class, source);
        int lengthAfterOne = source.linkedVariants.length;
        // registering into a second provider appends a distinct variant without detaching the first
        ObjectWriterProvider p2 = new ObjectWriterProvider();
        p2.register(LinkTargets.class, source);
        assertEquals(lengthAfterOne + 1, source.linkedVariants.length);

        // a reconciling re-link of the same variant adds nothing twice
        int snapshotLength = source.linkedVariants.length;
        for (ObjectWriterAdapter variant : new ArrayList<>(liveVariants(source.linkedVariants))) {
            source.linkSortedVariant(variant);
        }
        assertEquals(snapshotLength, source.linkedVariants.length);
    }

    @Test
    public void concurrentLinkAndFilterUpdateConverge() throws Exception {
        // a filter update racing a link must end with the same filter on the variant,
        // whatever the interleaving
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < 50; round++) {
                ObjectWriterAdapter source = (ObjectWriterAdapter) ObjectWriters.objectWriter(LinkTargets.class,
                        ObjectWriters.fieldWriter("beta", String.class, (LinkTargets t) -> t.beta),
                        ObjectWriters.fieldWriter("alpha", String.class, (LinkTargets t) -> t.alpha));
                ValueFilter identity = (object, name, value) -> value;
                ValueFilter mask = (object, name, value) -> "password".equals(name) ? "***" : value;
                source.setValueFilter(identity);

                ObjectWriterAdapter variant = new ObjectWriterAdapter(
                        LinkTargets.class, null, null, 0L, source.getFieldWriters());
                CountDownLatch start = new CountDownLatch(1);
                Future<?> linker = pool.submit(() -> {
                    start.await();
                    source.linkSortedVariant(variant);
                    return null;
                });
                Future<?> setter = pool.submit(() -> {
                    start.await();
                    source.setValueFilter(mask);
                    return null;
                });
                start.countDown();
                linker.get(30, TimeUnit.SECONDS);
                setter.get(30, TimeUnit.SECONDS);

                // identity is a prefix of every later update, so the converged state must be masked
                assertSame(mask, source.valueFilter);
                assertSame(mask, variant.valueFilter);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void maskedOutputOnLinkedVariantProperty() {
        ValueFilter mask = (object, name, value) -> "password".equals(name) ? "***" : value;
        ObjectWriter writer = ObjectWriters.objectWriter(RentCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (RentCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (RentCreds s) -> s.name));
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(RentCreds.class, writer);
        writer.setValueFilter(mask);
        assertEquals("{\"name\":\"alice\",\"password\":\"***\"}",
                JSON.toJSONString(new RentCreds(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @Test
    public void renamedOutputOnLinkedVariantProperty() {
        // a NameFilter set on the registered writer must rename through the linked sorted variant
        NameFilter mask = (object, name, value) -> "password".equals(name) ? "secret" : name;
        ObjectWriter writer = ObjectWriters.objectWriter(RentCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (RentCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (RentCreds s) -> s.name));
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(RentCreds.class, writer);
        ((ObjectWriterAdapter) writer).setNameFilter(mask);
        assertEquals("{\"name\":\"alice\",\"secret\":\"hunter2\"}",
                JSON.toJSONString(new RentCreds(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @Test
    public void droppedPropertyOnLinkedVariantPropertyPre() {
        // a PropertyPreFilter set on the registered writer must drop through the linked variant
        // process(JSONWriter, Object, String) selects by field name
        PropertyPreFilter mask = (writer, source, fieldName) -> !"password".equals(fieldName);
        ObjectWriter writer = ObjectWriters.objectWriter(RentCreds.class,
                ObjectWriters.fieldWriter("password", String.class, (RentCreds s) -> s.password),
                ObjectWriters.fieldWriter("name", String.class, (RentCreds s) -> s.name));
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(RentCreds.class, writer);
        ((ObjectWriterAdapter) writer).setPropertyPreFilter(mask);
        assertEquals("{\"name\":\"alice\"}",
                JSON.toJSONString(new RentCreds(),
                        new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically)));
    }

    @Test
    public void moduleWritersLinkAcrossPublication() {
        // a module returning a fresh instance per call still links each published cell to the
        // cached counterpart, so a filter set on the natural writer reaches the sorted twin
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(new com.alibaba.fastjson2.modules.ObjectWriterModule() {
            @Override
            public ObjectWriter getObjectWriter(java.lang.reflect.Type objectType, Class objectClass) {
                if (objectClass == LinkTargets.class) {
                    return ObjectWriters.objectWriter(LinkTargets.class,
                            ObjectWriters.fieldWriter("beta", String.class, (LinkTargets t) -> t.beta),
                            ObjectWriters.fieldWriter("alpha", String.class, (LinkTargets t) -> t.alpha));
                }
                return null;
            }
        });
        LinkTargets value = new LinkTargets();
        JSON.toJSONString(value, new JSONWriter.Context(provider));
        JSON.toJSONString(value, new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically));
        ObjectWriter natural = provider.getObjectWriter(LinkTargets.class, LinkTargets.class, 0L);
        ((ObjectWriterAdapter) natural).setNameFilter((object, name, fieldValue) -> "beta".equals(name) ? "b" : name);
        String json = JSON.toJSONString(value,
                new JSONWriter.Context(provider, JSONWriter.Feature.SortFieldNamesAlphabetically));
        assertEquals("{\"alpha\":\"a\",\"b\":\"b\"}", json);
    }
}
