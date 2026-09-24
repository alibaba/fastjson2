package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A Java type is identified by its name and the ClassLoader that loaded it, so a class named
 * {@code com.alibaba.fastjson2.util.DynamicClassLoader} loaded by another ClassLoader is not the
 * same type as this one. When the thread context ClassLoader (TCCL) holds a second fastjson2 copy,
 * using it as the parent of the dynamic ClassLoader makes the generated writers resolve types such
 * as FieldWriterList from that other copy, which fails while creating an ObjectWriter. See gh-7869.
 */
@Tag("regression")
class Issue7869 {
    @Test
    public void foreignDynamicClassLoaderInContextClassLoader() throws Exception {
        ClassLoader foreignClassLoader = new ForeignFastjson2ClassLoader(getClass().getClassLoader());
        // sanity check: the foreign ClassLoader really resolves a second copy of fastjson2
        Class<?> foreignDynamicClassLoader = foreignClassLoader.loadClass(DynamicClassLoader.class.getName());
        assertNotSame(DynamicClassLoader.class, foreignDynamicClassLoader);

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(foreignClassLoader);
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader();
            assertSame(DynamicClassLoader.class.getClassLoader(), dynamicClassLoader.getParent());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void contextClassLoaderOfSameFastjson2IsStillUsed() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader sameFastjson2ClassLoader = new ClassLoader(contextClassLoader) {
        };
        try {
            Thread.currentThread().setContextClassLoader(sameFastjson2ClassLoader);
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader();
            assertSame(sameFastjson2ClassLoader, dynamicClassLoader.getParent());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void createObjectWriterWithForeignDynamicClassLoaderInContextClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new ForeignFastjson2ClassLoader(contextClassLoader));

            ObjectWriterCreatorASM creator = new ObjectWriterCreatorASM(new DynamicClassLoader());
            ObjectWriter objectWriter = creator.createObjectWriter(Bean.class);

            Bean bean = new Bean();
            Item item = new Item();
            item.id = 101;
            bean.items = Arrays.asList(item);

            byte[] jsonb;
            try (JSONWriter jsonWriter = JSONWriter.ofJSONB()) {
                objectWriter.write(jsonWriter, bean);
                jsonb = jsonWriter.getBytes();
            }

            Bean parsed = JSONB.parseObject(jsonb, Bean.class);
            assertEquals(1, parsed.items.size());
            assertEquals(101, parsed.items.get(0).id);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static class Bean {
        public List<Item> items;
    }

    public static class Item {
        public int id;
    }

    /**
     * Stands for the ClassLoader of a second fastjson2 copy: classes of {@code com.alibaba.fastjson2}
     * are loaded by this ClassLoader itself instead of being delegated to the parent. The test
     * classes are excluded so that only the fastjson2 library is a second copy.
     */
    static class ForeignFastjson2ClassLoader
            extends ClassLoader {
        private static final String PREFIX = "com.alibaba.fastjson2.";
        private static final String TEST_PREFIX = "com.alibaba.fastjson2.issues_7800.";

        ForeignFastjson2ClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith(PREFIX) && !name.startsWith(TEST_PREFIX)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    byte[] bytes = readClassBytes(name);
                    if (bytes != null) {
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    }
                }
                if (loaded != null) {
                    return loaded;
                }
            }
            return super.loadClass(name, resolve);
        }

        private byte[] readClassBytes(String name) {
            ClassLoader parent = getParent();
            if (parent == null) {
                return null;
            }

            try (InputStream in = parent.getResourceAsStream(name.replace('.', '/') + ".class")) {
                if (in == null) {
                    return null;
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                return out.toByteArray();
            } catch (IOException e) {
                return null;
            }
        }
    }
}
