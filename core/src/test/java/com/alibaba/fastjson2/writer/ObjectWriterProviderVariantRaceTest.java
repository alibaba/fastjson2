package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Replays, in a fixed order, the races between creating, linking and publishing the two field-order variants of a
 * bean writer: a provider subclass pauses the thread creating the sorted writer at its lookups of the natural cell.
 */
public class ObjectWriterProviderVariantRaceTest {
    @JSONType(alphabetic = false)
    public static class Account {
        public String password = "hunter2";
        public String name = "alice";
        public int id = 7;
    }

    static final ValueFilter MASK = (object, name, value) -> "password".equals(name) ? "***" : value;
    static final String MASKED = "{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}";

    static final class PausingProvider extends ObjectWriterProvider {
        volatile Thread paused;
        volatile Runnable onNaturalLookup;
        final AtomicInteger naturalLookups = new AtomicInteger();

        PausingProvider() {
            super(ObjectWriterCreator.INSTANCE);
        }

        @Override
        ConcurrentMap<Type, ObjectWriter> cacheOf(boolean fieldBased, boolean fieldNamesSorted) {
            ConcurrentMap<Type, ObjectWriter> map = super.cacheOf(fieldBased, fieldNamesSorted);
            return fieldNamesSorted || Thread.currentThread() != paused ? map : new PausingView(map, this);
        }

        boolean sortedPublished() {
            return super.cacheOf(false, true).containsKey(Account.class);
        }

        String sortedWrite() {
            return JSON.toJSONString(new Account(), new JSONWriter.Context(this, JSONWriter.Feature.SortFieldNamesAlphabetically));
        }
    }

    static final class PausingView extends AbstractMap<Type, ObjectWriter> implements ConcurrentMap<Type, ObjectWriter> {
        final ConcurrentMap<Type, ObjectWriter> map;
        final PausingProvider provider;

        PausingView(ConcurrentMap<Type, ObjectWriter> map, PausingProvider provider) {
            this.map = map;
            this.provider = provider;
        }

        @Override
        public ObjectWriter get(Object key) {
            ObjectWriter value = map.get(key);
            if (key == Account.class) {
                provider.naturalLookups.incrementAndGet();
                provider.onNaturalLookup.run();
            }
            return value;
        }

        @Override
        public Set<Map.Entry<Type, ObjectWriter>> entrySet() {
            return map.entrySet();
        }

        @Override
        public ObjectWriter put(Type key, ObjectWriter value) {
            return map.put(key, value);
        }

        @Override
        public ObjectWriter putIfAbsent(Type key, ObjectWriter value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return map.remove(key, value);
        }

        @Override
        public boolean replace(Type key, ObjectWriter oldValue, ObjectWriter newValue) {
            return map.replace(key, oldValue, newValue);
        }

        @Override
        public ObjectWriter replace(Type key, ObjectWriter value) {
            return map.replace(key, value);
        }
    }

    static void await(CountDownLatch latch) {
        boolean reached;
        try {
            reached = latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        org.junit.jupiter.api.Assertions.assertTrue(reached,
                "latch wait timed out: the interleaving the test exists for was never reached");
    }

    static void awaitPublished(PausingProvider provider) {
        // a bounded spin: the regression this guards must fail the test, not hang the fork
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!provider.sortedPublished() && System.nanoTime() < deadline) {
            Thread.yield();
        }
        org.junit.jupiter.api.Assertions.assertTrue(provider.sortedPublished(),
                "the sorted writer was never published");
    }

    @Test
    public void filterOnExistingNaturalWriterIsCopiedBeforeSortedPublication() throws Exception {
        PausingProvider provider = new PausingProvider();
        provider.getObjectWriter(Account.class, Account.class, false).setFilter(MASK);

        // B creates the sorted writer and is paused at its look at the natural cell; C writes sorted as soon as
        // the sorted writer is published, or tells B to go on after seeing it unpublished
        CountDownLatch bPaused = new CountDownLatch(1);
        CountDownLatch cTurnTaken = new CountDownLatch(1);
        provider.onNaturalLookup = () -> {
            if (provider.naturalLookups.get() == 1) {
                bPaused.countDown();
                await(cTurnTaken);
            }
        };
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> b = pool.submit(() -> {
                provider.paused = Thread.currentThread();
                return provider.sortedWrite();
            });
            await(bPaused);
            if (!provider.sortedPublished()) {
                cTurnTaken.countDown();
                awaitPublished(provider);
            }
            String c = provider.sortedWrite();
            cTurnTaken.countDown();
            assertEquals(MASKED, c);
            assertEquals(MASKED, b.get(10, TimeUnit.SECONDS));
            org.junit.jupiter.api.Assertions.assertEquals(1, provider.naturalLookups.get(),
                    "the pause point at the first natural-cell look was not exercised");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void filterSetWhileBothVariantsAreCreatedReachesTheSortedWriter() throws Exception {
        PausingProvider provider = new PausingProvider();

        // B starts the first sorted write and is paused right after its single look at the (empty)
        // natural cell. A then creates the natural writer and sets a filter on it. Once B resumes it
        // publishes the sorted writer, and C's sorted write starts only after the filter was set: it
        // must be masked, because publication links the counterpart's filters first.
        CountDownLatch bPaused = new CountDownLatch(1);
        CountDownLatch aDone = new CountDownLatch(1);
        Thread[] a = new Thread[1];
        provider.onNaturalLookup = () -> {
            if (provider.naturalLookups.get() == 1) {
                bPaused.countDown();
                // let A run to completion, or until it waits for this thread
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (aDone.getCount() > 0 && System.nanoTime() < deadline
                        && (a[0] == null || a[0].getState() != Thread.State.BLOCKED)) {
                    Thread.yield();
                }
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> b = pool.submit(() -> {
                provider.paused = Thread.currentThread();
                return provider.sortedWrite();
            });
            await(bPaused);
            Future<?> natural = pool.submit(() -> {
                a[0] = Thread.currentThread();
                provider.getObjectWriter(Account.class, Account.class, false).setFilter(MASK);
                aDone.countDown();
                return null;
            });
            natural.get(10, TimeUnit.SECONDS);
            awaitPublished(provider);
            String c = provider.sortedWrite();
            b.get(10, TimeUnit.SECONDS);
            assertEquals(MASKED, c);
            assertEquals(MASKED, provider.sortedWrite());
        } finally {
            pool.shutdownNow();
        }
    }
}
