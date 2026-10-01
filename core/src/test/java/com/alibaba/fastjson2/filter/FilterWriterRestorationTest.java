package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilterWriterRestorationTest {
    @Test
    public void beforeFilterRestoresWriterAfterNestedFailure() {
        try (JSONWriter outer = JSONWriter.of(); JSONWriter inner = JSONWriter.of()) {
            BeforeFilter filter = new BeforeFilter() {
                @Override
                public void writeBefore(Object object) {
                    if (Boolean.TRUE.equals(object)) {
                        throw new IllegalStateException("nested failure");
                    }
                    assertThrows(IllegalStateException.class, () -> writeBefore(inner, Boolean.TRUE));
                    writeKeyValue("value", 123);
                }
            };
            outer.startObject();
            inner.startObject();
            filter.writeBefore(outer, Boolean.FALSE);
            outer.endObject();
            inner.endObject();
            assertEquals("{\"value\":123}", outer.toString());
            assertEquals("{}", inner.toString());
        }
    }

    @Test
    public void afterFilterRestoresWriterAfterNestedFailure() {
        try (JSONWriter outer = JSONWriter.of(); JSONWriter inner = JSONWriter.of()) {
            AfterFilter filter = new AfterFilter() {
                @Override
                public void writeAfter(Object object) {
                    if (Boolean.TRUE.equals(object)) {
                        throw new IllegalStateException("nested failure");
                    }
                    assertThrows(IllegalStateException.class, () -> writeAfter(inner, Boolean.TRUE));
                    writeKeyValue("value", 123);
                }
            };
            outer.startObject();
            inner.startObject();
            filter.writeAfter(outer, Boolean.FALSE);
            outer.endObject();
            inner.endObject();
            assertEquals("{\"value\":123}", outer.toString());
            assertEquals("{}", inner.toString());
        }
    }
}
