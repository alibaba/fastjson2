package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3736 {
    @Test
    public void test() {
        assertEquals(JSON.toJSONString(new TestData()), "{}");
        assertEquals(JSON.toJSONString(new TestData2()), "{\"aia\":[],\"al\":[],\"ala\":[],\"b\":false}");
        assertEquals(JSON.toJSONString(new TestData3()), "{\"aia\":[],\"al\":[],\"ala\":[],\"b\":false,\"o\":{}}");
    }

    @Data
    public class TestData {
        private AtomicLongArray ala;
        private AtomicIntegerArray aia;
        private ArrayList al;
        private Boolean b;
        private Object o;
    }

    @Data
    public class TestData2 {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        private AtomicLongArray ala;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        private AtomicIntegerArray aia;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullListAsEmpty)
        private ArrayList al;

        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullBooleanAsFalse)
        private Boolean b;
    }

    @Data
    public class TestData3 {
        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private AtomicLongArray ala;

        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private AtomicIntegerArray aia;

        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private ArrayList al;

        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Boolean b;

        @JSONField(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
        private Object o;
    }
}
