package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest3 {
    @Test
    public void test_ref_0() {
        JSONObject a = new JSONObject();
        a.put("ref", a);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONObject obj = JSONB.parseObject(bytes, JSONObject.class);
        assertSame(obj, obj.get("ref"));

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b, b.ref);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertSame(a1, a1.ref);
    }

    @Test
    public void test_ref_0_creators() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            JSONObject a = new JSONObject();
            a.put("ref", a);

            byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

            JSONObject obj = JSONB.parseObject(bytes, JSONObject.class);
            assertSame(obj, obj.get("ref"));

            ObjectReader<B> objectReader = creator.createObjectReader(B.class);
            B b = objectReader.readJSONBObject(JSONReader.ofJSONB(bytes), null, null, 0);

            assertSame(b, b.ref);

            A a1 = JSONB.parseObject(bytes, A.class);
            assertSame(a1, a1.ref);
        }
    }

    public static class A {
        private A ref;

        public A getRef() {
            return ref;
        }

        public void setRef(A ref) {
            this.ref = ref;
        }
    }

    private static class B {
        public B ref;
    }

    @Test
    public void test_ref_1() {
        JSONObject a = new JSONObject();
        JSONArray b = new JSONArray();
        a.put("b", b);
        b.add(a);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONObject obj = JSONB.parseObject(bytes, JSONObject.class);
        assertSame(obj, obj.getJSONArray("b").get(0));
    }

    @Test
    public void test_ref_2() {
        JSONArray a = new JSONArray();
        JSONArray b = new JSONArray();
        a.add(b);
        b.add(a);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONArray array = JSONB.parseObject(bytes, JSONArray.class);
        assertSame(array, array.getJSONArray(0).getJSONArray(0));
    }
}
