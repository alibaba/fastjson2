package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3759 {
    @Test
    public void testWriteNullBooleanAsFalse() {
        TestDTO dto = new TestDTO();
        String json = JSON.toJSONString(dto);
        assertTrue(json.contains("\"booleanField\":false"), "Expected booleanField to be false, but got: " + json);
    }

    @Test
    public void testWriteNullBooleanAsFalseReflect() {
        TestDTO dto = new TestDTO();
        String json = ObjectWriterCreator.INSTANCE.createObjectWriter(TestDTO.class).toJSONString(dto);
        assertTrue(json.contains("\"booleanField\":false"), "Expected booleanField to be false, but got: " + json);
    }

    @Test
    public void testWriteNullBooleanAsFalseWithContext() {
        TestDTO dto = new TestDTO();
        String json = JSON.toJSONString(dto, JSONWriter.Feature.WriteNullBooleanAsFalse);
        System.out.println("Serialized JSON with context feature: " + json);

        // 检查是否包含booleanField字段且值为false
        assertTrue(json.contains("\"booleanField\":false"), "Expected booleanField to be false with context feature, but got: " + json);
    }

    public static class TestDTO {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNullBooleanAsFalse)
        private Boolean booleanField;

        private String otherField = "";

        public Boolean getBooleanField() {
            return booleanField;
        }

        public void setBooleanField(Boolean booleanField) {
            this.booleanField = booleanField;
        }

        public String getOtherField() {
            return otherField;
        }

        public void setOtherField(String otherField) {
            this.otherField = otherField;
        }
    }
}
