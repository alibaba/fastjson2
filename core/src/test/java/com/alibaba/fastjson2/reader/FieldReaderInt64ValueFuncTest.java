package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderInt64ValueFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123L, bean.value);
        assertNotNull(fieldReader.method);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101L,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean {
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        ObjectReader<Bean1> objectReader = TestUtils.createObjectReaderLambda(Bean1.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123D));

        assertEquals(
                201L,
                objectReader.readObject(
                        JSONReader.of("{\"value\":201}"),
                        0
                ).value
        );
    }

    public static class Bean1 {
        @JSONField(schema = "{'minimum':128}")
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = TestUtils.createObjectReaderLambda(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(Exception.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123D));
    }

    public static class Bean2 {
        public void setValue(long value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        ObjectReader<Bean3> objectReader = TestUtils.createObjectReaderLambda(Bean3.class);
        assertEquals(
                123L,
                objectReader.readObject(
                        JSONReader.of("{\"id\":101, \"value\":123}")
                ).value
        );
    }

    public static class Bean3 {
        private long value;
        public final int id;

        public Bean3(@JSONField(name = "id") int id) {
            this.id = id;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }
}
