package com.alibaba.fastjson2.aliyun;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.writer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeSortTest {
    @Test
    public void test() {
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(C0.class);
        List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
        assertEquals(1, fieldWriters.size());
        assertEquals(C0.class, fieldWriters.get(0).method.getDeclaringClass());
    }

    @Test
    public void testReader() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(C0.class);
        FieldReader[] fieldReaders = ((ObjectReaderAdapter) objectReader).getFieldReaders();
        assertEquals(1, fieldReaders.length);
        assertEquals(C0.class, fieldReaders[0].method.getDeclaringClass());
    }

    public static class P0 {
        public int id;
    }

    public static class C0
            extends P0 {
        public int getId() {
            return id;
        }

        public void setId(int value) {
            this.id = value;
        }
    }

    @Test
    public void test1() {
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(C1.class);
        List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
        assertEquals(1, fieldWriters.size());
        assertEquals(C1.class, fieldWriters.get(0).method.getDeclaringClass());
    }

    @Test
    public void testReader1() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(C1.class);
        FieldReader[] fieldReaders = ((ObjectReaderAdapter) objectReader).getFieldReaders();
        assertEquals(1, fieldReaders.length);
        assertEquals(C1.class, fieldReaders[0].method.getDeclaringClass());
    }

    public static class P1 {
        public int getId() {
            return 0;
        }

        public void setId(int value) {
        }
    }

    public static class C1
            extends P1 {
        public int getId() {
            return 1;
        }

        public void setId(int value) {
        }
    }

    @Test
    public void test2() {
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(C2.class);
        List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
        assertEquals(1, fieldWriters.size());
        assertEquals(C2.class, fieldWriters.get(0).field.getDeclaringClass());
    }

    // Android not support
    // GraalVM not support
    @Test
    public void test2Lambda() {
        ObjectWriter objectWriter = ObjectWriterCreatorLambda.INSTANCE.createObjectWriter(C2.class);
        List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
        assertEquals(1, fieldWriters.size());
        assertEquals(C2.class, fieldWriters.get(0).field.getDeclaringClass());
    }

    // Android not support
    // GraalVM not support
    @Test
    public void test2ASM() {
        ObjectWriter objectWriter = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(C2.class);
        List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
        assertEquals(1, fieldWriters.size());
        assertEquals(C2.class, fieldWriters.get(0).field.getDeclaringClass());
    }

    @Test
    public void testReader2() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(C2.class);
        FieldReader[] fieldReaders = ((ObjectReaderAdapter) objectReader).getFieldReaders();
        assertEquals(1, fieldReaders.length);
        assertEquals(C2.class, fieldReaders[0].field.getDeclaringClass());
    }

    public static class P2 {
        public int id;
    }

    public static class C2
            extends P2 {
        public int id;
    }
}
