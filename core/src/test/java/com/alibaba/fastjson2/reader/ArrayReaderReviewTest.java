package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArrayReaderReviewTest {
    @Test
    public void genericArrayReaderAcceptsJsonbNull() {
        Type type = new TypeReference<List<String>[]>() { }.getType();
        ObjectReader reader = new ObjectReaderProvider().getObjectReader(type);
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes(null))) {
            assertNull(reader.readJSONBObject(json, type, null, 0));
        }
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes(new Object[0]))) {
            assertEquals(0, ((List[]) reader.readJSONBObject(json, type, null, 0)).length);
        }
    }

    @Test
    public void charArrayBuilderIsAppliedToJsonbStrings() {
        ObjectReader reader = new ObjectReaderImplCharValueArray(String::new);
        try (JSONReader json = JSONReader.ofJSONB(JSONB.toBytes("abc"))) {
            assertEquals("abc", reader.readJSONBObject(json, null, null, 0));
        }
        try (JSONReader json = JSONReader.of("\"abc\"")) {
            assertEquals("abc", reader.readObject(json));
        }
    }
}
