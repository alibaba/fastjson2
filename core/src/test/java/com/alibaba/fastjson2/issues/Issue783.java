package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.read.ClassLoaderTest;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue783 {
    @Test
    public void test() {
        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(Bean.class);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(Bean.class);
        assertSame(objectWriter, objectWriter1);
        writerProvider.cleanup(Bean.class);
        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(Bean.class);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void test1() {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(Bean.class);
        ObjectReader objectReader1 = readerProvider.getObjectReader(Bean.class);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(Bean.class);
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

        writerProvider.cleanup(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(objectClass);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void testWriteMap() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type mapType = TypeReference.mapType(Map.class, String.class, objectClass);

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(mapType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(mapType);
        assertSame(objectWriter, objectWriter1);

        writerProvider.cleanup(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(mapType);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void testWriteMap1() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type mapType = TypeReference.mapType(Map.class, objectClass, String.class);

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(mapType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(mapType);
        assertSame(objectWriter, objectWriter1);

        writerProvider.cleanup(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(mapType);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void testWriteCollection() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type listType = TypeReference.collectionType(List.class, objectClass);

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(listType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(listType);
        assertSame(objectWriter, objectWriter1);

        objectWriter.write(JSONWriter.of(), Arrays.asList(objectClass.newInstance()), null, null, 0);
        writerProvider.cleanup(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(listType);
        assertSame(objectWriter, objectWriter2);
    }

    @Test
    public void testWriteOptional() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type optionalType = TypeReference.parametricType(Optional.class, objectClass);

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(optionalType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(optionalType);
        assertSame(objectWriter, objectWriter1);

        writerProvider.cleanup(classLoader);

        ObjectWriter objectWriter2 = writerProvider.getObjectWriter(optionalType);
        assertNotSame(objectWriter, objectWriter2);
    }

    @Test
    public void testWriteParamType1() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type paramType = TypeReference.parametricType(Bean1.class, TypeReference.parametricType(Optional.class, objectClass));

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(paramType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(paramType);
        assertSame(objectWriter, objectWriter1);

        Bean1 bean = new Bean1();
        bean.value = Optional.of(objectClass.newInstance());
        objectWriter1.write(JSONWriter.of(), bean, null, null, 0);

        writerProvider.cleanup(classLoader);
    }

    @Test
    public void testWriteParamType2() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type paramType = TypeReference.parametricType(Bean2.class, TypeReference.collectionType(List.class, objectClass));

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        ObjectWriter objectWriter = writerProvider.getObjectWriter(paramType);
        ObjectWriter objectWriter1 = writerProvider.getObjectWriter(paramType);
        assertSame(objectWriter, objectWriter1);

        Bean2 bean = new Bean2();
        bean.value = Arrays.asList(objectClass.newInstance());
        objectWriter1.write(JSONWriter.of(), bean, null, null, 0);

        writerProvider.cleanup(classLoader);
    }

    @Test
    public void testClass() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(objectClass);
        ObjectReader objectReader1 = readerProvider.getObjectReader(objectClass);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(objectClass);
        assertNotSame(objectReader, objectReader2);
    }

    @Test
    public void test4() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type listType = TypeReference.collectionType(List.class, objectClass);

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(listType);
        ObjectReader objectReader1 = readerProvider.getObjectReader(listType);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(listType);
        assertNotSame(objectReader, objectReader2);
    }

    @Test
    public void test5() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type listType = TypeReference.mapType(Map.class, String.class, objectClass);

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(listType);
        ObjectReader objectReader1 = readerProvider.getObjectReader(listType);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(listType);
        assertNotSame(objectReader, objectReader2);
    }

    @Test
    public void test6() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type listType = TypeReference.mapType(Map.class, objectClass, String.class);

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(listType);
        ObjectReader objectReader1 = readerProvider.getObjectReader(listType);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(listType);
        assertNotSame(objectReader, objectReader2);
    }

    @Test
    public void test7() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type paramType = TypeReference.parametricType(Optional.class, objectClass);

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(paramType);
        ObjectReader objectReader1 = readerProvider.getObjectReader(paramType);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(paramType);
        assertNotSame(objectReader, objectReader2);
    }

    @Test
    public void testReaderParam1() throws Exception {
        ClassLoaderTest.ExtClassLoader classLoader = new ClassLoaderTest.ExtClassLoader();
        Class objectClass = classLoader.loadClass("com.alibaba.mock.demo.api.Demo");
        Type paramType = TypeReference.parametricType(Bean1.class, TypeReference.parametricType(Optional.class, objectClass));

        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = readerProvider.getObjectReader(paramType);
        ObjectReader objectReader1 = readerProvider.getObjectReader(paramType);
        assertSame(objectReader, objectReader1);

        readerProvider.cleanup(classLoader);
        ObjectReader objectReader2 = readerProvider.getObjectReader(paramType);
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

    public static class Bean1<T> {
        public Optional<T> value;
    }

    public static class Bean2<T> {
        public List<T> value;
    }
}
