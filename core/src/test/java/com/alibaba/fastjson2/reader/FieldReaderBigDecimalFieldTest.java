package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldReaderBigDecimalFieldTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        ObjectReader objectReader = JSONFactory.defaultObjectReaderProvider.getObjectReader(Bean.class);
        FieldReader fieldReader = objectReader.getFieldReader("value");
        fieldReader.accept(bean, "123");
        assertEquals(new BigDecimal("123"), bean.value);

        assertThrows(JSONException.class, () -> fieldReader.accept(bean, new Object()));
    }

    private static class Bean {
        public BigDecimal value;
    }
}
