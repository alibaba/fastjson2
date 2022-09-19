package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.read.ClassLoaderTest;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue783 {
    @Test
    public void test() {
        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(Bean.class);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(Bean.class);
        assertSame(objectWriter, objectWriter1);
        writerProvider.cleanUp(Bean.class);
        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(Bean.class);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void test1() {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(Bean.class);
        ObjectReader objectReader1 = readerProvider.getObjectReader(Bean.class);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanUp(Bean.class);
        ObjectReader objectReader2 = readerProvider.getObjectReader(Bean.class);
        assertNotSame(objectReader, objectReader2);
    }

    public static class Bean {
    }

    @Test
    public void test2() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(objectClass);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(objectClass);
        assertSame(objectWriter, objectWriter1);

        writerProvider.cleanUp(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(objectClass);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void test3() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(objectClass);
        ObjectReader objectReader1 = readerProvider.getObjectReader(objectClass);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanUp(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(objectClass);
        assertNotSame(objectReader, objectReader2);
    }

    public static class ExtClassLoader
            extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());

            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/Demo.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.mock.demo.api.Demo", bytes, 0, bytes.length);
            }
            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("external/MockDemoService.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();

                super.defineClass("com.alibaba.mock.demo.service.MockDemoService", bytes, 0, bytes.length);
            }
        }
    }
}
