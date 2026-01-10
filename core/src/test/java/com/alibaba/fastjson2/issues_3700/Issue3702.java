package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3702 {
    @Test
    public void test() {
        String expected = "{\"d\":0.0,\"f\":0.0,\"i\":0,\"l\":0}";
        assertEquals(expected, JSON.toJSONString(new NumberDTO()));
        assertEquals(expected, new String(JSON.toJSONBytes(new NumberDTO())));
        assertEquals(expected, JSON.toJSONString(new NumberDTO2()));
        assertEquals(expected, new String(JSON.toJSONBytes(new NumberDTO2())));
    }

    @Test
    public void test4() {
        String expected = "{\"bigDecimal\":0.0,\"bigInteger\":0,\"byteValue\":0,\"doubleValue\":0.0,\"floatValue\":0.0,\"intValue\":0,\"longValue\":0,\"number\":0,\"shortValue\":0}";
        assertEquals(expected, JSON.toJSONString(new NumberDTO4()));
        assertEquals(expected, new String(JSON.toJSONBytes(new NumberDTO4())));
    }

    @Test
    public void test5() {
        String expected = "{\"bigDecimal\":0.0,\"bigInteger\":0,\"byteValue\":0,\"doubleValue\":0.0,\"floatValue\":0.0,\"intValue\":0,\"longValue\":0,\"number\":0,\"shortValue\":0}";
        assertEquals(expected, JSON.toJSONString(new NumberDTO5()));
        assertEquals(expected, new String(JSON.toJSONBytes(new NumberDTO5())));
    }

    @Test
    public void test6() {
        String expected = "{\"bigDecimal\":0.0,\"bigInteger\":0,\"byteValue\":0,\"doubleValue\":0.0,\"floatValue\":0.0,\"intValue\":0,\"longValue\":0,\"number\":0,\"shortValue\":0}";
        assertEquals(expected, JSON.toJSONString(new NumberDTO3(), JSONWriter.Feature.WriteNullNumberAsZero));
    }

    @Test
    public void test3() {
        assertEquals("{}", JSON.toJSONString(new NumberDTO3()));
        assertEquals("{}", new String(JSON.toJSONBytes(new NumberDTO3())));

        String expected = "{\"bigDecimal\":0.0,\"bigInteger\":0,\"byteValue\":0,\"doubleValue\":0.0,\"floatValue\":0.0,\"intValue\":0,\"longValue\":0,\"number\":0,\"shortValue\":0}";
        assertEquals(expected, JSON.toJSONString(new NumberDTO3(), JSONWriter.Feature.WriteNullNumberAsZero));
        assertEquals(expected, new String(JSON.toJSONBytes(new NumberDTO3(), JSONWriter.Feature.WriteNullNumberAsZero)));
    }

    @Data
    public class NumberDTO
            implements Serializable {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Integer i;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Double d;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Float f;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Long l;
    }

    @Data
    public class NumberDTO2
            implements Serializable {
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Integer i;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Double d;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Float f;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Long l;
    }

    @Data
    public class NumberDTO3
            implements Serializable {
        private Byte byteValue;
        private Short shortValue;
        private Integer intValue;
        private Long longValue;
        private Float floatValue;
        private Double doubleValue;
        private BigDecimal bigDecimal;
        private BigInteger bigInteger;
        private Number number;
    }

    @Data
    public class NumberDTO4
            implements Serializable {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private BigDecimal bigDecimal;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private BigInteger bigInteger;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Byte byteValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Short shortValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Integer intValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Long longValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Float floatValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Double doubleValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullNumberAsZero)
        private Number number;
    }

    @Data
    public class NumberDTO5
            implements Serializable {
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private BigDecimal bigDecimal;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private BigInteger bigInteger;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Byte byteValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Short shortValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Integer intValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Long longValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Float floatValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Double doubleValue;
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Number number;
    }
}
