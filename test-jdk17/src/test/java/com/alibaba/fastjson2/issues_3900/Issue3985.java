package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3985 {
    public record Money(Long value) {
        public Money divide(Money divisor) {
            if (divisor == null || divisor.value == 0) {
                throw new ArithmeticException("The divisor must not be null or zero.");
            }
            return this;
        }
    }

    @Test
    public void testRecordJsonbSetterMatch() {
        JSONObject object = new JSONObject();
        object.put("value", 100L);
        object.put("divide", null);
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteNulls);

        Money money = assertDoesNotThrow(() -> JSONB.parseObject(bytes, Money.class));
        assertEquals(100L, money.value());
    }
}
