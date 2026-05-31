package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("reader")
public class ObjectReaderProviderBugFixTest {
    // =========================================================================
    // Bug 1: registerSeeAlsoSubType used subTypeClass as cache key instead of superClass
    // =========================================================================

    @JSONType(typeKey = "type", seeAlso = DogAnimal.class, seeAlsoDefault = DogAnimal.class)
    public abstract static class Animal {
        public String name;
    }

    @JSONType(typeName = "Dog")
    public static class DogAnimal extends Animal {
    }

    @JSONType(typeName = "Cat")
    public static class CatAnimal extends Animal {
    }

    @JSONType(typeName = "Bird")
    public static class BirdAnimal extends Animal {
    }

    @Test
    public void testRegisterSeeAlsoSubType_cacheKey() {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        ObjectReader readerBefore = provider.getObjectReader(Animal.class);
        assertInstanceOf(ObjectReaderSeeAlso.class, readerBefore);

        provider.registerSeeAlsoSubType(CatAnimal.class, "Cat");

        ObjectReader readerAfter = provider.getObjectReader(Animal.class);
        assertInstanceOf(ObjectReaderSeeAlso.class, readerAfter);
        assertNotSame(readerBefore, readerAfter, "Reader should be updated for Animal.class");

        Animal cat = (Animal) readerAfter.readObject(
                JSONReader.of("{\"type\":\"Cat\"}"), null, null, 0);
        assertInstanceOf(CatAnimal.class, cat);
    }

    @Test
    public void testRegisterSeeAlsoSubType_fieldBasedFallback() {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        ObjectReader readerBefore = provider.getObjectReader(Animal.class, true);
        assertInstanceOf(ObjectReaderSeeAlso.class, readerBefore);

        provider.cache.remove(Animal.class);

        provider.registerSeeAlsoSubType(BirdAnimal.class, "Bird");

        ObjectReader readerAfter = provider.cacheFieldBased.get(Animal.class);
        assertNotNull(readerAfter, "Reader should be cached under Animal.class, not BirdAnimal.class");
        assertInstanceOf(ObjectReaderSeeAlso.class, readerAfter);
    }

    // =========================================================================
    // Bug 2: afterAutoType called with null clazz → NPE in autoTypeHandler
    //        Two locations: !autoTypeSupport whitelist path and autoTypeSupport fallthrough
    // =========================================================================

    @Test
    public void testCheckAutoType_nonExistentClass_autoTypeSupport_noNPE() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        List<Class> receivedTypes = Collections.synchronizedList(new ArrayList<>());
        provider.setAutoTypeHandler(receivedTypes::add);

        Class<?> result = provider.checkAutoType(
                "com.nonexistent.ClassName12345",
                null,
                JSONReader.Feature.SupportAutoType.mask
        );

        assertNull(result);
        assertTrue(receivedTypes.isEmpty(), "autoTypeHandler should not be called with null class");
    }

    @Test
    public void testCheckAutoType_nonExistentClass_whitelist_noNPE() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        List<Class> receivedTypes = Collections.synchronizedList(new ArrayList<>());
        provider.setAutoTypeHandler(receivedTypes::add);

        provider.addAutoTypeAccept("com.whitetest.");

        Class<?> result = provider.checkAutoType(
                "com.whitetest.NoSuchClass999",
                null,
                0
        );

        assertNull(result);
        assertTrue(receivedTypes.isEmpty(), "autoTypeHandler should not be called with null class");
    }

    @Test
    public void testCheckAutoType_existentClass_handlerCalled() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        List<Class> receivedTypes = Collections.synchronizedList(new ArrayList<>());
        provider.setAutoTypeHandler(receivedTypes::add);

        Class<?> result = provider.checkAutoType(
                "java.lang.String",
                String.class,
                0
        );

        assertEquals(String.class, result);
        assertEquals(1, receivedTypes.size());
        assertEquals(String.class, receivedTypes.get(0));
    }

    // =========================================================================
    // Bug 3: acceptHashCodes — volatile for read visibility,
    //        synchronized + local cache for write-write safety and consistency
    // =========================================================================

    @Test
    public void testAddAutoTypeAccept_visibility() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        provider.addAutoTypeAccept("com.visibility.TestClass");

        AtomicInteger found = new AtomicInteger(0);
        Thread reader = new Thread(() -> {
            try {
                Field f = ObjectReaderProvider.class.getDeclaredField("acceptHashCodes");
                f.setAccessible(true);
                long[] codes = (long[]) f.get(provider);
                long hash = Fnv.hashCode64("com.visibility.TestClass");
                if (java.util.Arrays.binarySearch(codes, hash) >= 0) {
                    found.set(1);
                }
            } catch (Exception e) {
                // ignore
            }
        });
        reader.start();
        reader.join();

        assertEquals(1, found.get(), "Another thread should see the added accept entry");
    }

    @Test
    public void testAddAutoTypeAccept_concurrent() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        int threadCount = 8;
        int itemsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerThread; i++) {
                        provider.addAutoTypeAccept("com.test.thread" + threadId + ".Class" + i);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await();

        assertEquals(0, errorCount.get(), "No exceptions during concurrent addAutoTypeAccept");

        Field f = ObjectReaderProvider.class.getDeclaredField("acceptHashCodes");
        f.setAccessible(true);
        long[] acceptHashCodes = (long[]) f.get(provider);

        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < itemsPerThread; i++) {
                String name = "com.test.thread" + t + ".Class" + i;
                long hash = Fnv.hashCode64(name);
                assertTrue(
                        java.util.Arrays.binarySearch(acceptHashCodes, hash) >= 0,
                        "Entry should be present: " + name
                );
            }
        }
    }

    @Test
    public void testAddAutoTypeAccept_idempotent() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();

        provider.addAutoTypeAccept("com.test.Foo");
        Field f = ObjectReaderProvider.class.getDeclaredField("acceptHashCodes");
        f.setAccessible(true);
        int lengthAfterFirst = ((long[]) f.get(provider)).length;

        provider.addAutoTypeAccept("com.test.Foo");
        int lengthAfterSecond = ((long[]) f.get(provider)).length;

        assertEquals(lengthAfterFirst, lengthAfterSecond, "Duplicate add should not grow the array");
    }
}
