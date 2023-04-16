package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderBooleanValueMethodTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "true");
        assertEquals(true, bean.value);
        assertNotNull(fieldReader.method);

        fieldReader.accept(bean, 0);
        assertEquals(false, bean.value);

        fieldReader.accept(bean, 1);
        assertEquals(true, bean.value);

        fieldReader.accept(bean, false);
        assertEquals(false, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                true,
                objectReader.readObject(
                        JSONReader.of("{\"value\":true}"),
                        0
                ).value
        );

        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes));
        assertEquals(bean.value, bean1.value);
    }

    public static class Bean {
        private boolean value;

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(Exception.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, (short) 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123D));
    }

    public static class Bean2 {
        public void setValue(boolean value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean3.class);
        assertEquals(
                true,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":true}")
                ).value
        );
    }

    public static class Bean3 {
        private boolean value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }
}
