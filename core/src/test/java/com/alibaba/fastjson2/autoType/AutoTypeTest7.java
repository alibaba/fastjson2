package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest7 {
    @Test
    public void test_0() throws Exception {
        A a = new A();
        a.value = new C(1001);

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest7$C\",\"id\":1001}}", json);

        A a2 = JSON.parseObject(json, A.class);
        assertSame(a2.value.getClass(), C.class);
    }

    @Test
    public void test_1() throws Exception {
        A1 a = new A1();
        a.value = new C(1001);

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest7$C\",\"id\":1001}}", json);

        A1 a2 = JSON.parseObject(json, A1.class);
        assertSame(a2.value.getClass(), C.class);
    }

    @Test
    public void test_2() throws Exception {
        A1 a = new A1();
        a.value = new C(1001);

        ObjectReaderCreator[] readerCreators = TestUtils.readerCreators();

        ObjectWriterCreator[] writerCreators = TestUtils.writerCreators();

        for (ObjectWriterCreator writerCreator : writerCreators) {
            ObjectWriter objectWriter = writerCreator.createObjectWriter(A1.class);
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
            jsonWriter.setRootObject(a);
            objectWriter.write(jsonWriter, a);
            String json = jsonWriter.toString();
            assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest7$C\",\"id\":1001}}", json);
        }

        String json = JSON.toJSONString(a, JSONWriter.Feature.NotWriteRootClassName, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"value\":{\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest7$C\",\"id\":1001}}", json);

        for (ObjectReaderCreator readerCreator : readerCreators) {
            ObjectReader<A1> objectReader = readerCreator.createObjectReader(A1.class);
            JSONReader jsonReader = JSONReader.of(json);
            A1 a2 = objectReader.readObject(jsonReader);
            assertSame(a2.value.getClass(), C.class);
        }
    }

    public static class A {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType)
        public B value;
    }

    public static class A1 {
        private B value;

        public B getValue() {
            return value;
        }

        @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType)
        public void setValue(B value) {
            this.value = value;
        }
    }

    public static class B {
        public B() {
        }
    }

    public static class C
            extends B {
        public int id;

        public C() {
        }

        public C(int id) {
            this.id = id;
        }
    }
}
