package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToFloatFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FloatFunctionWriterReviewTest {
    @Test
    void floatFunctionRetainsPrimitiveType() {
        FieldWriter field = ObjectWriterCreator.INSTANCE.createFieldWriter("value", (ToFloatFunction<Float>) value -> value);
        assertSame(float.class, field.fieldClass);
        assertSame(float.class, field.fieldType);
        ObjectWriter writer = ObjectWriterCreator.INSTANCE.createObjectWriter(field);
        try (JSONWriter jsonWriter = JSONWriter.of()) {
            writer.write(jsonWriter, 1.25F);
            assertEquals("{\"value\":1.25}", jsonWriter.toString());
        }
    }
}
