package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2952 {
    @Test
    public void toJsonList() {
        assertEquals("{\"d\":null,\"i\":null,\"l\":null,\"s\":null}", JSON.toJSONString(new C()));
    }

    @Data
    public class C {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        private Long l;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        private Double d;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        private Integer i;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        private String s;
    }
}
