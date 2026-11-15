package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.read.ClassLoaderTest;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("regression")
public class Issue7896 {
    private static final String DEMO = "com.alibaba.mock.demo.api.Demo";

    @Test
    public void clearRemovesAutoTypeReaderButKeepsBuiltInReaders() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(Bean.class);
        long hashCode = Fnv.hashCode64(Bean.class.getName());
        provider.registerIfAbsent(hashCode, objectReader);
        assertNotNull(provider.getObjectReader(hashCode));

        provider.clear();

        assertNull(provider.getObjectReader(hashCode));
        assertNotNull(provider.getObjectReader(Fnv.hashCode64(String.class.getName())));
    }

    @Test
    public void cleanupClassLoaderKeepsReadersOfOtherClassLoaders() throws Exception {
        ClassLoaderTest.ExtClassLoader first = new ClassLoaderTest.ExtClassLoader();
        ClassLoaderTest.ExtClassLoader second = new ClassLoaderTest.ExtClassLoader();
        ObjectReaderProvider provider = new ObjectReaderProvider();

        long firstHashCode = Fnv.hashCode64("first");
        long secondHashCode = Fnv.hashCode64("second");
        long beanHashCode = Fnv.hashCode64(Bean.class.getName());
        provider.registerIfAbsent(firstHashCode, provider.getObjectReader(first.loadClass(DEMO)));
        provider.registerIfAbsent(secondHashCode, provider.getObjectReader(second.loadClass(DEMO)));
        provider.registerIfAbsent(beanHashCode, provider.getObjectReader(Bean.class));

        provider.cleanup(first);

        assertNull(provider.getObjectReader(firstHashCode));
        assertNotNull(provider.getObjectReader(secondHashCode));
        assertNotNull(provider.getObjectReader(beanHashCode));
    }

    @Test
    public void cleanupClassKeepsReadersOfOtherClasses() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        long beanHashCode = Fnv.hashCode64(Bean.class.getName());
        long otherHashCode = Fnv.hashCode64(Other.class.getName());
        provider.registerIfAbsent(beanHashCode, provider.getObjectReader(Bean.class));
        provider.registerIfAbsent(otherHashCode, provider.getObjectReader(Other.class));

        provider.cleanup(Bean.class);

        assertNull(provider.getObjectReader(beanHashCode));
        assertNotNull(provider.getObjectReader(otherHashCode));
    }

    @Test
    public void cleanupClassLoaderRemovesContainerReaderOfItsClass() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass(DEMO);

        ObjectReaderProvider provider = new ObjectReaderProvider();
        Type listOfDemo = new ParameterizedTypeImpl(List.class, objectClass);
        long hashCode = Fnv.hashCode64("list of demo");
        provider.registerIfAbsent(hashCode, provider.getObjectReader(listOfDemo));
        assertNotNull(provider.getObjectReader(hashCode));

        provider.cleanup(classLoader);

        assertNull(provider.getObjectReader(hashCode));
    }

    @Test
    public void cleanupClassRemovesCustomReaderRegisteredByTypeName() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.addAutoTypeAccept(Custom.class.getName());
        ObjectReader objectReader = provider.getObjectReader(
                Custom.class.getName(), Object.class, JSONReader.Feature.SupportAutoType.mask
        );
        assertNotNull(objectReader);
        // a custom reader does not know its class
        assertNull(objectReader.getObjectClass());
        long hashCode = Fnv.hashCode64(Custom.class.getName());
        assertSame(objectReader, provider.getObjectReader(hashCode));

        provider.cleanup(Custom.class);

        assertNull(provider.getObjectReader(hashCode));
    }

    @Test
    public void readerCachePairOfAnOlderGenerationIsDropped() throws Exception {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(Bean.class);
        long hashCode = Fnv.hashCode64(Bean.class.getName());
        provider.registerIfAbsent(hashCode, objectReader);

        Field generationField = ObjectReaderProvider.class.getDeclaredField("readerCacheGeneration");
        generationField.setAccessible(true);
        int generation = generationField.getInt(null);

        provider.cleanup(Bean.class);

        // what a lookup that read the reader before the cleanup publishes afterwards
        Class<?> pairClass = Class.forName(ObjectReaderProvider.class.getName() + "$ObjectReaderCachePair");
        Constructor<?> constructor = pairClass.getDeclaredConstructor(long.class, ObjectReader.class, int.class);
        constructor.setAccessible(true);
        Field readerCacheField = ObjectReaderProvider.class.getDeclaredField("readerCache");
        readerCacheField.setAccessible(true);
        readerCacheField.set(null, constructor.newInstance(hashCode, objectReader, generation));

        assertNull(provider.getObjectReader(hashCode));
    }

    @JSONType(deserializer = CustomReader.class)
    public static class Custom {
    }

    public static class CustomReader
            implements ObjectReader {
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return new Custom();
        }
    }

    public static class Other {
    }

    @Test
    public void cleanupClassLoaderRemovesAutoTypeReader() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");

        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(objectClass);
        long hashCode = Fnv.hashCode64(objectClass.getName());
        provider.registerIfAbsent(hashCode, objectReader);
        assertNotNull(provider.getObjectReader(hashCode));

        provider.cleanup(classLoader);

        assertNull(provider.getObjectReader(hashCode));
    }

    @Test
    public void cleanupClassRemovesAutoTypeReader() {
        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(Bean.class);
        long hashCode = Fnv.hashCode64(Bean.class.getName());
        provider.registerIfAbsent(hashCode, objectReader);
        assertNotNull(provider.getObjectReader(hashCode));

        provider.cleanup(Bean.class);

        assertNull(provider.getObjectReader(hashCode));
    }

    @Test
    public void cleanupClassLoaderRemovesReaderRegisteredUnderOtherContextClassLoader() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");

        ObjectReaderProvider provider = new ObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(objectClass);
        long hashCode = Fnv.hashCode64(objectClass.getName());

        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(new OtherClassLoader(contextClassLoader));
        try {
            provider.registerIfAbsent(hashCode, objectReader);
            assertNotNull(provider.getObjectReader(hashCode));

            provider.cleanup(classLoader);

            assertNull(provider.getObjectReader(hashCode));
        } finally {
            thread.setContextClassLoader(contextClassLoader);
        }
    }

    public static class OtherClassLoader
            extends ClassLoader {
        public OtherClassLoader(ClassLoader parent) {
            super(parent);
        }
    }

    public static class Bean {
    }
}
