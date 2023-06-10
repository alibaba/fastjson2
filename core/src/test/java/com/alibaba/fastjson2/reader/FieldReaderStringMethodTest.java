package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderStringMethodTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals("123", bean.value);
        assertNotNull(fieldReader.method);

        fieldReader.accept(bean, null);
        assertNull(bean.value);

        assertEquals(
                "101",
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );

        assertEquals(
                "abc",
                objectReader.readObject(
                        JSONReader.of("{\"value\":\" abc \"}"),
                        0
                ).value
        );
    }

    private static class Bean {
        @JSONField(format = "trim")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123D));
    }

    private static class Bean2 {
        public void setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean3.class);
        assertEquals(
                "123",
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );

        assertEquals(
                "abc",
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":\" abc \"}")
                ).value
        );
    }

    private static class Bean3 {
        @JSONField(deserializeFeatures = JSONReader.Feature.TrimString)
        private String value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
