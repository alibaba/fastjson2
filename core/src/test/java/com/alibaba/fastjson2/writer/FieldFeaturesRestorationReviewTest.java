package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldFeaturesRestorationReviewTest {
    @Test
    public void failingGetterRestoresWriterFeatures() {
        FieldWriter field = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class).getFieldWriter("value");
        try (JSONWriter writer = JSONWriter.of(JSONWriter.Feature.WriteNulls)) {
            long features = writer.getFeatures();
            assertThrows(RuntimeException.class, () -> field.write(writer, new Bean()));
            assertEquals(features, writer.getFeatures());
        }
    }

    public static class Bean {
        @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
        public Object getValue() {
            throw new IllegalStateException();
        }
    }
}
