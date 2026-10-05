package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DelimitedInputReviewTest {
    @Test
    void finalRecordSurvivesBufferGrowthAndCompaction() {
        char[] content = new char[20000];
        Arrays.fill(content, 'x');
        String value = new String(content);
        for (String prefix : new String[]{"", "\"first\"\n"}) {
            String text = prefix + JSON.toJSONString(value);
            List<String> expected = prefix.isEmpty() ? Arrays.asList(value) : Arrays.asList("first", value);
            List<String> values = new ArrayList<>();
            JSON.<String>parseObject(new StringReader(text), '\n', String.class, values::add);
            assertEquals(expected, values);
            values.clear();
            JSON.<String>parseObject(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), String.class, values::add);
            assertEquals(expected, values);
        }
    }

    @Test
    void consumesFinalRecordWithoutDelimiter() {
        for (String text : new String[] {"1\n2", "1\n2\n"}) {
            List<Integer> values = new ArrayList<>();
            JSON.<Integer>parseObject(new StringReader(text), '\n', Integer.class, values::add);
            assertEquals(JSONArray.of(1, 2), values);
            values.clear();
            JSON.<Integer>parseObject(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), Integer.class, values::add);
            assertEquals(JSONArray.of(1, 2), values);
        }
    }

    @Test
    void readerResolvesReferencesAndRejectsTrailingContent() {
        List<JSONObject> values = new ArrayList<>();
        JSON.<JSONObject>parseObject(new StringReader("{\"a\":{},\"b\":{\"$ref\":\"$.a\"}}\n"), '\n', JSONObject.class, values::add);
        assertSame(values.get(0).get("a"), values.get(0).get("b"));
        assertThrows(JSONException.class, () -> JSON.parseObject(new StringReader("1 2\n"), '\n', Integer.class, v -> {}));
    }
}
