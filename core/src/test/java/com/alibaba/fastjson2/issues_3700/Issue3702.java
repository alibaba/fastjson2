package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3702 {
    @Test
    public void test() {
        assertEquals(JSON.toJSONString(new NumberDTO()), "{\"d\":0,\"f\":0,\"i\":0,\"l\":0}");
        assertEquals(JSON.toJSONString(new NumberDTO2()), "{\"d\":0.0,\"f\":0.0,\"i\":0,\"l\":0}");
        assertEquals(JSON.toJSONString(new NumberDTO3()), "{}");
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
        private Integer integer;
        private Double wrapperDouble;
        private Float wrapperFloat;
        private Long wrapperLong;
    }
}
