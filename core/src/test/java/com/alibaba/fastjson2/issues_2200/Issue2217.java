package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2217 {
    @Test
    public void test() {
        PrometheusQueryRangeValue bean = JSON.parseObject("[123, \"abc\"]", PrometheusQueryRangeValue.class);
        assertEquals(123, bean.id);
        assertEquals("abc", bean.value);
    }

    public static class PrometheusQueryRangeValueDeserializer
            extends JsonDeserializer<PrometheusQueryRangeValue>
            implements ObjectReader<PrometheusQueryRangeValue> {
        @Override
        public PrometheusQueryRangeValue deserialize(
                JsonParser jsonParser,
                DeserializationContext ctx) throws IOException {
            throw new IOException();
        }

        @Override
        public PrometheusQueryRangeValue readObject(
                JSONReader jsonReader,
                Type fieldType,
                Object fieldName,
                long features) {
            JSONArray array = jsonReader.readJSONArray();
            PrometheusQueryRangeValue value = new PrometheusQueryRangeValue();
            value.id = array.getIntValue(0);
            value.value = array.getString(1);
            return value;
        }
    }

    @JsonDeserialize(contentUsing = PrometheusQueryRangeValueDeserializer.class)
    public static class PrometheusQueryRangeValue {
        public int id;
        public String value;
    }
}
