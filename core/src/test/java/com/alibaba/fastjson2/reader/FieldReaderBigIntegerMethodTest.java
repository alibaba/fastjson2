package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldReaderBigIntegerMethodTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(new BigInteger("123"), bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));
    }

    private static class Bean {
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean1.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONSchemaValidException.class, () -> fieldReader.accept(bean, 123L));
    }

    private static class Bean1 {
        @JSONField(schema = "{'minimum':128}")
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123));
        assertThrows(JSONException.class, () -> fieldReader.accept(bean, 123L));
    }

    private static class Bean2 {
        public void setValue(BigInteger value) {
            throw new UnsupportedOperationException();
        }
    }
}
