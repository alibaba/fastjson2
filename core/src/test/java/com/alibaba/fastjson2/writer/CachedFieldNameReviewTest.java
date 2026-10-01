package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachedFieldNameReviewTest {
    @Test
    public void specialNamesRoundTripThroughCachedUtf8AndUtf16() {
        for (String name : new String[] {"quote\"", "slash\\", "line\n", "zero\0", "emoji\uD83D\uDE00"}) {
            ObjectWriter<Bean> objectWriter = ObjectWriters.of(Bean.class,
                    ObjectWriters.fieldWriter(name, (java.util.function.ToIntFunction<Bean>) bean -> bean.value));
            try (JSONWriter writer = JSONWriter.ofUTF8()) {
                objectWriter.write(writer, new Bean());
                assertEquals(JSONObject.of(name, 123), JSON.parseObject(writer.getBytes()));
            }
            try (JSONWriter writer = JSONWriter.ofUTF16()) {
                objectWriter.write(writer, new Bean());
                assertEquals(JSONObject.of(name, 123), JSON.parseObject(writer.toString()));
            }
        }
    }

    @Test
    public void annotatedMethodOutranksUnannotatedFieldSymmetrically() throws Exception {
        FieldWriter field = ObjectWriters.fieldWriter("value", Bean.class.getField("value"));
        FieldWriter method = ObjectWriters.fieldWriter("value", Bean.class.getMethod("getOther"));
        assertTrue(field.compareTo(method) > 0);
        assertTrue(method.compareTo(field) < 0);
    }

    public static class Bean {
        public int value = 123;

        @JSONField(name = "value")
        public int getOther() {
            return 456;
        }
    }
}
