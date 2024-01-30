package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ValueConsumerEmptyTest {
    @Test
    public void test() {
        ValueConsumer v = new ValueConsumer() {};
        v.accept(new byte[0], 0, 0);
        v.accept(0);
        v.accept(0L);
        v.accept(JSONArray.of());
        v.accept(JSONObject.of());
        v.accept(BigDecimal.ONE);
        v.accept(true);
        v.acceptNull();
    }
}
