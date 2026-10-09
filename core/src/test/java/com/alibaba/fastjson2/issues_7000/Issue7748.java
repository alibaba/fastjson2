package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.issues_7000.Issue7748Bean.Generator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue7748 {
    private IsolatedClassLoader classLoader;

    @BeforeEach
    public void setUp() {
        classLoader = new IsolatedClassLoader();
        // ASM reader defined by the test class loader, while beans are loaded by IsolatedClassLoader
        JSONFactory.setContextReaderCreator(new ObjectReaderCreatorASM(Issue7748.class.getClassLoader()));
    }

    @AfterEach
    public void tearDown() {
        JSONFactory.setContextReaderCreator(null);
    }

    @Test
    public void objectField() throws Exception {
        Class<?> beanClass = isolated(Issue7748Bean.class);
        Object bean = JSON.parseObject("{\"generator\":{}}", beanClass);

        Object generator = beanClass.getMethod("getGenerator").invoke(bean);
        assertSame(isolated(Generator.class), generator.getClass());
    }

    @Test
    public void listItem() throws Exception {
        Class<?> beanClass = isolated(Issue7748ListBean.class);
        Object bean = JSON.parseObject("{\"generators\":{}}", beanClass);

        Collection<?> generators = (Collection<?>) beanClass.getMethod("getGenerators").invoke(bean);
        assertSame(isolated(Generator.class), generators.iterator().next().getClass());
    }

    private Class<?> isolated(Class<?> clazz) throws ClassNotFoundException {
        Class<?> isolated = classLoader.loadClass(clazz.getName());
        assertNotSame(clazz, isolated);
        return isolated;
    }

    /** Parent-last for Issue7748Bean*, simulating ParallelWebappClassLoader. */
    static class IsolatedClassLoader
            extends ClassLoader {
        IsolatedClassLoader() {
            super(Issue7748.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith(Issue7748Bean.class.getName())
                    && !name.equals(Issue7748ListBean.class.getName())) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> clazz = findLoadedClass(name);
                if (clazz == null) {
                    byte[] bytes = read(name);
                    clazz = defineClass(name, bytes, 0, bytes.length);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
        }

        private byte[] read(String name) throws ClassNotFoundException {
            String resource = name.replace('.', '/') + ".class";
            try (InputStream in = getParent().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new ClassNotFoundException(name);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                for (int len; (len = in.read(buf)) != -1;) {
                    out.write(buf, 0, len);
                }
                return out.toByteArray();
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }
}
