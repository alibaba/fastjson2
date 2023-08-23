package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1562 {
    @Test
    public void test() {
        String expected = "{\"value\":null,\"value10\":null,\"value11\":null,\"value12\":null,\"value2\":null,\"value3\":null,\"value4\":null,\"value5\":null,\"value6\":null,\"value7\":null,\"value8\":null,\"value9\":null}";
        assertEquals(expected, JSON.toJSONString(new Cs()));
    }

    @Data
    class Cs {
        @JSONField(format = "#.##")
        private double value = Double.NaN;
        @JSONField(format = "#.##")
        private float value2 = Float.NaN;
        @JSONField(format = "#.##")
        private double value3 = Double.NEGATIVE_INFINITY;
        @JSONField(format = "#.##")
        private float value4 = Float.NEGATIVE_INFINITY;
        @JSONField(format = "#.##")
        private double value5 = Double.POSITIVE_INFINITY;
        @JSONField(format = "#.##")
        private float value6 = Float.POSITIVE_INFINITY;
        @JSONField
        private double value7 = Double.NaN;
        @JSONField
        private float value8 = Float.NaN;
        @JSONField
        private double value9 = Double.NEGATIVE_INFINITY;
        @JSONField
        private float value10 = Float.NEGATIVE_INFINITY;
        @JSONField
        private double value11 = Double.POSITIVE_INFINITY;
        @JSONField
        private float value12 = Float.POSITIVE_INFINITY;
    }
}
