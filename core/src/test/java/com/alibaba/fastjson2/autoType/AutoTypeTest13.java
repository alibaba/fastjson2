package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Differ;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest13 {
    @Test
    public void test_0() throws Exception {
        Money bean = new Money(Currency.getInstance("CNY"));

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        Money bean2 = (Money) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.value);

        Differ.diff(bean, bean2);
    }

    public static class Money {
        public final Currency value;

        public Money(Currency value) {
            this.value = value;
        }
    }
}
