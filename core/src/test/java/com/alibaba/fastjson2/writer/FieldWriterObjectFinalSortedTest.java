package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("writer")
public class FieldWriterObjectFinalSortedTest {
    public static final class FinalMap extends LinkedHashMap<String, String> {
    }

    public static class Holder {
        private final FinalMap map = new FinalMap();

        @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
        public FinalMap getMap() {
            return map;
        }
    }

    @Test
    public void sortedResolutionIsNotStoredOnTheFieldWriter() {
        // a final Map subclass reaches FieldWriterObjectFinal only through the lambda/JIT overload;
        // the Map exclusion is the parent's type-carrying rule and must not apply here, or a sorted
        // resolution is written into the cross-context objectWriter field
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriterAdapter adapter = (ObjectWriterAdapter) provider.getObjectWriter(Holder.class, Holder.class);
        FieldWriterObjectFinal fieldWriter = null;
        for (Object item : adapter.fieldWriters) {
            FieldWriter writer = (FieldWriter) item;
            if ("map".equals(writer.fieldName)) {
                fieldWriter = (FieldWriterObjectFinal) writer;
            }
        }
        assertNotNull(fieldWriter);
        assertNull(fieldWriter.objectWriter);

        try (JSONWriter sortedWriter = JSONWriter.of(new JSONWriter.Context(
                provider, JSONWriter.Feature.SortFieldNamesAlphabetically))) {
            assertNotNull(fieldWriter.getObjectWriter(sortedWriter, FinalMap.class));
            assertNull(fieldWriter.objectWriter,
                    "a sorted resolution must not populate the cross-context field writer cache");

            try (JSONWriter naturalWriter = JSONWriter.of(new JSONWriter.Context(provider))) {
                ObjectWriter natural = fieldWriter.getObjectWriter(naturalWriter, FinalMap.class);
                assertNotNull(natural);
                assertSame(natural, fieldWriter.objectWriter);
            }
        }
    }
}
