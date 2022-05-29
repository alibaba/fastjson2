package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1764_bean_biginteger {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals("{\"value\":\"9007199254741992\"}",
                JSON.toJSONString(
                        new Model(9007199254741992L), JSONWriter.Feature.BrowserCompatible));

        assertEquals("{\"value\":\"-9007199254741992\"}",
                JSON.toJSONString(
                        new Model(-9007199254741992L), JSONWriter.Feature.BrowserCompatible));

        assertEquals("{\"value\":9007199254740990}",
                JSON.toJSONString(
                        new Model(9007199254740990L), JSONWriter.Feature.BrowserCompatible));

        assertEquals("{\"value\":-9007199254740990}",
                JSON.toJSONString(
                        new Model(-9007199254740990L), JSONWriter.Feature.BrowserCompatible));

        assertEquals("{\"value\":100}",
                JSON.toJSONString(
                        new Model(100), JSONWriter.Feature.BrowserCompatible));

        assertEquals("{\"value\":-100}",
                JSON.toJSONString(
                        new Model(-100), JSONWriter.Feature.BrowserCompatible));
    }

    public static class Model {
        public BigInteger value;

        public Model() {
        }

        public Model(long value) {
            this.value = BigInteger.valueOf(value);
        }
    }
}
