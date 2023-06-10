package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderBigIntegerFuncTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader<Bean> objectReader = TestUtils.createObjectReaderLambda(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(new BigInteger("123"), bean.value);
        assertNotNull(fieldReader.method);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));

        assertEquals(
                new BigInteger("101"),
                objectReader.readObject(
                        JSONReader.of("{\"value\":101}"),
                        0
                ).value
        );
    }

    public static class Bean {
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
        ObjectReader objectReader = TestUtils.createObjectReaderLambda(Bean2.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        assertThrows(Exception.class, () -> fieldReader.accept(bean, "123"));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123));
        assertThrows(Exception.class, () -> fieldReader.accept(bean, 123L));
    }

    public static class Bean2 {
        public void setValue(BigInteger value) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test3() {
        Bean3 bean = new Bean3();
        bean.value = BigInteger.valueOf(256);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":256}", str);
        Bean3 bean1 = JSON.parseObject(str, Bean3.class);
        assertEquals(bean.value, bean1.value);

        Bean3 bean2 = JSON.parseObject(str).toJavaObject(Bean3.class);
        assertEquals(bean.value, bean2.value);
    }

    private static class Bean3 {
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
    }
}
