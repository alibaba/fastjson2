package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderFloatValueFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(123F, bean.value);
        assertNotNull(fieldReader.method);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                101F,
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
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
                201F,
                objectReader.readObject(
                        JSONReader.of("{\"value\":201}"),
                        0
                ).value
        );
    }

    @Test
    public void test1_refelct() {
        Bean1 bean = new Bean1();
        ObjectReader<Bean1> objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(Bean1.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123L));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123F));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123D));

        assertEquals(
                201F,
                objectReader.readObject(
                        JSONReader.of("{\"value\":201}"),
                        0
                ).value
        );
    }

    public static class Bean1 {
        @JSONField(schema = "{'minimum':128}")
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
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
        public void setValue(float value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void readFieldValue() {
        Object fieldValue = ObjectReaders.fieldReaderFloat("value", Bean2::setValue)
                .readFieldValue(JSONReader.of("12.34"));
        assertEquals(
                12.34F, ((Float) fieldValue).floatValue()
        );
    }
}
