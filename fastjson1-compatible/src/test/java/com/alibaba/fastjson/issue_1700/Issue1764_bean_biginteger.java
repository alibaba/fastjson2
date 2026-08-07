package com.alibaba.fastjson.issue_1700;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static com.alibaba.fastjson.serializer.SerializerFeature.BrowserCompatible;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1764_bean_biginteger {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals("{\"value\":\"9007199254741992\"}",
                JSON.toJSONString(
                        new Model(9007199254741992L), BrowserCompatible));

        assertEquals("{\"value\":\"-9007199254741992\"}",
                JSON.toJSONString(
                        new Model(-9007199254741992L), BrowserCompatible));

        assertEquals("{\"value\":9007199254740990}",
                JSON.toJSONString(
                        new Model(9007199254740990L), BrowserCompatible));

        assertEquals("{\"value\":-9007199254740990}",
                JSON.toJSONString(
                        new Model(-9007199254740990L), BrowserCompatible));

        assertEquals("{\"value\":100}",
                JSON.toJSONString(
                        new Model(100), BrowserCompatible));

        assertEquals("{\"value\":-100}",
                JSON.toJSONString(
                        new Model(-100), BrowserCompatible));
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
