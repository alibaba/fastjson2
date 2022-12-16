package com.alibaba.fastjson2.fieldbased;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldBasedTest {
    @Test
    public void test_0() {
        ObjectWriterCreator writerCreator = ObjectWriterCreator.INSTANCE;
        ObjectReaderCreator readerCreator = ObjectReaderCreator.INSTANCE;

        A a = new A();
        a.id = 101;

        ObjectWriter<A> objectWriter = writerCreator.createObjectWriter(A.class, JSONWriter.Feature.FieldBased.mask, JSONFactory.getDefaultObjectWriterProvider());
        JSONWriter jsonWriter = JSONWriter.of();
        objectWriter.write(jsonWriter, a);

        String json = jsonWriter.toString();
        assertEquals("{\"id\":101}", json);

        ObjectReader<A> objectReader = readerCreator.createObjectReader(A.class, true);
        A a1 = objectReader.readObject(JSONReader.of(json), 0);
        assertEquals(a.id, a1.id);
    }

    @Test
    public void test_1() {
        A a = new A();
        a.id = 101;

        String json = JSON.toJSONString(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":101}", json);

        A a1 = JSON.parseObject(json, A.class, JSONReader.Feature.FieldBased);
        assertEquals(a.id, a1.id);
    }

    @Test
    public void test_1_jsonb() {
        A a = new A();
        a.id = 101;

        byte[] jsonbBytes = JSONB.toBytes(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":101}", JSONB.parseObject(jsonbBytes).toString());

        A a1 = JSONB.parseObject(jsonbBytes, A.class, JSONReader.Feature.FieldBased);
        assertEquals(a.id, a1.id);
    }

    static class A {
        private int id;
    }
}
