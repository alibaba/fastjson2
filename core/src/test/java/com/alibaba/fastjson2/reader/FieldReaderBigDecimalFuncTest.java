package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderBigDecimalFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(new BigDecimal("123"), bean.value);
        assertNotNull(fieldReader.method);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                new BigDecimal("101"),
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean {
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
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

        assertEquals(
                new BigDecimal("201"),
                objectReader.readObject(
                        JSONReader.of("{\"value\":201}"),
                        0
                ).value
        );
    }

    public static class Bean1 {
        @JSONField(schema = "{'minimum':128}")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
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
    }

    public static class Bean2 {
        public void setValue(BigDecimal value) {
            throw new UnsupportedOperationException();
        }
    }
}
