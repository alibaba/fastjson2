package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoType0 {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.BeanToArray,
            JSONWriter.Feature.FieldBased
    };

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.SupportArrayToBean,
            JSONReader.Feature.FieldBased
    };

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.id = 100;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean1.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean1.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean1 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id, bean1.id);
    }

    @Test
    public void test1_default() {
        Bean1 bean = new Bean1();
        bean.id = 100;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean1 bean1 = (Bean1) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id, bean1.id);
    }

    static class Bean1 {
        public int id;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.id0 = 1000;
        bean.id1 = 1001;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean2.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean2> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean2.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean2 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
    }

    @Test
    public void test2_default() {
        Bean2 bean = new Bean2();
        bean.id0 = 100;
        bean.id1 = 101;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean2 bean1 = (Bean2) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
    }

    static class Bean2 {
        public int id0;
        public int id1;
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.id0 = 1000;
        bean.id1 = 1001;
        bean.id2 = 1002;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean3.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean3> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean3.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean3 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
    }

    @Test
    public void test3_default() {
        Bean3 bean = new Bean3();
        bean.id0 = 100;
        bean.id1 = 101;
        bean.id2 = 102;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean3 bean1 = (Bean3) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
    }

    static class Bean3 {
        public int id0;
        public int id1;
        public int id2;
    }

    @Test
    public void test4() {
        Bean4 bean = new Bean4();
        bean.id0 = 1000;
        bean.id1 = 1001;
        bean.id2 = 1002;
        bean.id3 = 1003;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean4.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean4> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean4.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean4 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
    }

    @Test
    public void test4_default() {
        Bean4 bean = new Bean4();
        bean.id0 = 100;
        bean.id1 = 101;
        bean.id2 = 102;
        bean.id3 = 103;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean4 bean1 = (Bean4) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
    }

    static class Bean4 {
        public int id0;
        public int id1;
        public int id2;
        public int id3;
    }

    @Test
    public void test5() {
        Bean5 bean = new Bean5();
        bean.id0 = 1000;
        bean.id1 = 1001;
        bean.id2 = 1002;
        bean.id3 = 1003;
        bean.id4 = 1004;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean5.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean5> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean5.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean5 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
    }

    @Test
    public void test5_default() {
        Bean5 bean = new Bean5();
        bean.id0 = 100;
        bean.id1 = 101;
        bean.id2 = 102;
        bean.id3 = 103;
        bean.id4 = 104;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean5 bean1 = (Bean5) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
    }

    static class Bean5 {
        public int id0;
        public int id1;
        public int id2;
        public int id3;
        public int id4;
    }

    @Test
    public void test6() {
        Bean6 bean = new Bean6();
        bean.id0 = 1000;
        bean.id1 = 1001;
        bean.id2 = 1002;
        bean.id3 = 1003;
        bean.id4 = 1004;
        bean.id5 = 1005;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean6.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean6> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean6.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean6 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
        assertEquals(bean.id5, bean1.id5);
    }

    @Test
    public void test6_default() {
        Bean6 bean = new Bean6();
        bean.id0 = 100;
        bean.id1 = 101;
        bean.id2 = 102;
        bean.id3 = 103;
        bean.id4 = 104;
        bean.id5 = 105;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean6 bean1 = (Bean6) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
        assertEquals(bean.id5, bean1.id5);
    }

    static class Bean6 {
        public int id0;
        public int id1;
        public int id2;
        public int id3;
        public int id4;
        public int id5;
    }

    @Test
    public void test7() {
        Bean7 bean = new Bean7();
        bean.id0 = 1000;
        bean.id1 = 1001;
        bean.id2 = 1002;
        bean.id3 = 1003;
        bean.id4 = 1004;
        bean.id5 = 1005;
        bean.id6 = 1006;

        JSONWriter jsonWriter = JSONWriter.ofJSONB(writerFeatures);
        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean7.class);
        objectWriter.writeArrayMappingJSONB(jsonWriter, bean, null, null, 0);

        byte[] bytes = jsonWriter.getBytes();

        ObjectReader<Bean7> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean7.class);

        JSONReader jsonReader = JSONReader.ofJSONB(bytes, readerFeatures);

        Bean7 bean1 = objectReader.readArrayMappingJSONBObject(jsonReader, null, null, 0);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
        assertEquals(bean.id5, bean1.id5);
        assertEquals(bean.id6, bean1.id6);
    }

    @Test
    public void test7_default() {
        Bean7 bean = new Bean7();
        bean.id0 = 100;
        bean.id1 = 101;
        bean.id2 = 102;
        bean.id3 = 103;
        bean.id4 = 104;
        bean.id5 = 105;
        bean.id6 = 106;

        byte[] bytes = JSONB.toBytes(bean, writerFeatures);
        Bean7 bean1 = (Bean7) JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(bean.id0, bean1.id0);
        assertEquals(bean.id1, bean1.id1);
        assertEquals(bean.id2, bean1.id2);
        assertEquals(bean.id3, bean1.id3);
        assertEquals(bean.id4, bean1.id4);
        assertEquals(bean.id5, bean1.id5);
        assertEquals(bean.id6, bean1.id6);
    }

    static class Bean7 {
        public int id0;
        public int id1;
        public int id2;
        public int id3;
        public int id4;
        public int id5;
        public int id6;
    }
}
