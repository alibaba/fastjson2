package com.alibaba.fastjson2.v1issues.basicType;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.alibaba.fastjson.serializer.SerializerFeature.BrowserCompatible;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimal_type {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals("{\"value\":\"9007199254741992\"}",
                JSON.toJSONString(
                        new Model(9007199254741992L)));

        assertEquals("{\"value\":\"-9007199254741992\"}",
                JSON.toJSONString(
                        new Model(-9007199254741992L)));

        assertEquals("{\"value\":9007199254740990}",
                JSON.toJSONString(
                        new Model(9007199254740990L)));

        assertEquals("{\"value\":-9007199254740990}",
                JSON.toJSONString(
                        new Model(-9007199254740990L)));

        assertEquals("{\"value\":100}",
                JSON.toJSONString(
                        new Model(100)));

        assertEquals("{\"value\":-100}",
                JSON.toJSONString(
                        new Model(-100)));
    }

    @JSONType(serialzeFeatures = BrowserCompatible)
    public static class Model {
        public BigDecimal value;

        public Model() {
        }

        public Model(long value) {
            this.value = BigDecimal.valueOf(value);
        }
    }
}
