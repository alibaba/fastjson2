package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.MultiType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ArrayConversionReviewTest {
    @Test
    void multiTypeCollectionConvertsElements() {
        ObjectReader reader = new ObjectReaderProvider().getObjectReader(new MultiType(Integer.class, String.class));
        assertArrayEquals(new Object[] {12, "34"}, (Object[]) reader.createInstance(Arrays.asList("12", 34), 0));
        assertThrows(JSONException.class, () -> reader.createInstance(Arrays.asList(1, 2, 3), 0));
        try (JSONReader text = JSONReader.of("[1,2,3]");
                JSONReader binary = JSONReader.ofJSONB(JSONB.toBytes(new int[] {1, 2, 3}))) {
            assertThrows(JSONException.class, () -> reader.readObject(text));
            assertThrows(JSONException.class, () -> reader.readJSONBObject(binary, null, null, 0));
        }
    }

    @Test
    void finalArrayAcceptMatchesStreamingTruncation() throws Exception {
        FieldReader reader = ObjectReaderCreator.INSTANCE.createFieldReader("values", Bean.class.getField("values"));
        Bean bean = new Bean();
        reader.accept(bean, new int[] {1, 2, 3});
        assertArrayEquals(new int[] {1, 2}, bean.values);
        reader.accept(bean, (Object) null);
        assertArrayEquals(new int[] {1, 2}, bean.values);
    }

    @Test
    void localDateTimeReaderAdvertisesAcceptedConversions() throws Exception {
        FieldReader reader = ObjectReaderCreator.INSTANCE.createFieldReader("date", Bean.class.getField("date"));
        assertTrue(reader.supportAcceptType(Instant.class));
        assertTrue(reader.supportAcceptType(Long.class));
        assertFalse(reader.supportAcceptType(StringBuilder.class));
    }

    public static class Bean {
        public final int[] values = new int[2];
        public LocalDateTime date;
    }
}
