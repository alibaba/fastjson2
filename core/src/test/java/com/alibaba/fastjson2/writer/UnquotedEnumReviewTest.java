package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnquotedEnumReviewTest {
    @Test
    public void unquotedFieldNamesDoNotChangeEnumValueRepresentation() {
        ObjectWriterProvider provider = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        try (JSONWriter writer = JSONWriter.of(JSONFactory.createWriteContext(provider, JSONWriter.Feature.UnquoteFieldName))) {
            writer.writeAny(new Bean());
            assertEquals("{value:\"ONE\"}", writer.toString());
        }
        try (JSONWriter writer = JSONWriter.of(JSONFactory.createWriteContext(provider,
                JSONWriter.Feature.UnquoteFieldName, JSONWriter.Feature.WriteEnumUsingToString))) {
            writer.writeAny(new Bean());
            assertEquals("{value:\"custom\"}", writer.toString());
        }
    }

    public static class Bean {
        public Value value = Value.ONE;
    }

    public enum Value {
        ONE;

        @Override
        public String toString() {
            return "custom";
        }
    }
}
