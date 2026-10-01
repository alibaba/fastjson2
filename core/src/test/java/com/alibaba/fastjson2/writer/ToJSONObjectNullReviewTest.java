package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToJSONObjectNullReviewTest {
    @Test
    public void nullFormattedAndUnwrappedFieldsAreHandled() {
        ObjectWriterAdapter<Bean> writer = (ObjectWriterAdapter<Bean>) ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        assertTrue(writer.toJSONObject(new Bean()).isEmpty());
        JSONObject value = writer.toJSONObject(new Bean(), JSONWriter.Feature.WriteNulls.mask);
        assertEquals(JSONObject.of("date", null), value);
    }

    public static class Bean {
        @JSONField(format = "millis")
        public Date date;

        @JSONField(unwrapped = true)
        public Nested nested;
    }

    public static class Nested {
        public int value;
    }
}
