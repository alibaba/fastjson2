package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.read.ClassLoaderTest;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("regression")
public class Issue7896 {
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
