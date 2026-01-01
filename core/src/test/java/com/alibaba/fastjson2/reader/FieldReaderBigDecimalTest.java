package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class FieldReaderBigDecimalTest {
    @Test
    public void testBigDecimalField() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(BigDecimalBean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");

        BigDecimalBean bean = new BigDecimalBean();
        fieldReader.accept(bean, new BigDecimal("123.45"));
        assertEquals(new BigDecimal("123.45"), bean.value);

        fieldReader.accept(bean, "678.90");
        assertEquals(new BigDecimal("678.90"), bean.value);
    }

    @Test
    public void testBigDecimalReadField() {
        ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(BigDecimalBean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");

        BigDecimalBean bean = new BigDecimalBean();
        Object value = fieldReader.readFieldValue(JSONReader.of("{\"value\":123.45}"));
        // Test that reading works
        assertNotNull(value);
    }

    public static class BigDecimalBean {
        public BigDecimal value;
    }
}
