package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderInt32ValueMethodTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123, bean.value);
        assertNotNull(fieldReader.method);

        fieldReader.accept(bean, (short) 101);
        assertEquals(101, bean.value);

        fieldReader.accept(bean, 102);
        assertEquals(102, bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    @Test
    public void testJSONB() {
        Bean bean = new Bean();
        bean.value = 101;
        ObjectReader<Bean> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean.class);

        byte[] jsonbBytes = JSONB.toBytes(bean);
        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
        Bean bean1 = objectReader.readObject(jsonReader);
        assertEquals(bean.value, bean1.value);
    }

    public static class Bean {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void test1JSONB() {
        Bean1 bean = new Bean1();
        bean.value = 101;
        ObjectReader<Bean1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean1.class);

        byte[] jsonbBytes = JSONB.toBytes(bean);
        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
        Bean1 bean1 = objectReader.readObject(jsonReader);
        assertEquals(bean.value, bean1.value);
    }

    public static class Bean1 {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader<Bean2> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(Exception.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, (short) 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123D));
    }

    public static class Bean2 {
        public void setValue(int value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean3.class);
        assertEquals(
                (short) 123,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );
    }

    private static class Bean3 {
        private int value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
