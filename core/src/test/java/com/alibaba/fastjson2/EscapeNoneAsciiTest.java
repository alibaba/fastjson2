package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.util.JDKUtils.STRING_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscapeNoneAsciiTest {
    static final String[] STRINGS = new String[] {
            "中国", "中", "01234567中", "0123中", "01中"
    };

    static final String[] JSON_STRINGS = new String[] {
            "\"\\u4E2D\\u56FD\"", "\"\\u4E2D\"", "\"01234567\\u4E2D\"", "\"0123\\u4E2D\"", "\"01\\u4E2D\""
    };

    @Test
    public void testJSONWriterUTF16() {
        for (int i = 0; i < STRINGS.length; i++) {
            String STR = STRINGS[i];
            String JSON_STR = JSON_STRINGS[i];

            JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.EscapeNoneAscii);
            JSONWriter jsonWriter = new JSONWriterUTF16(context);
            jsonWriter.writeString(STR);
            String str = jsonWriter.toString();
            assertEquals(JSON_STR, str);
            assertEquals(STR, JSON.parse(str));
        }
    }

    @Test
    public void testJSONWriterUTF16JDK8() {
        for (int i = 0; i < STRINGS.length; i++) {
            String STR = STRINGS[i];
            String JSON_STR = JSON_STRINGS[i];
            JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.EscapeNoneAscii);
            JSONWriter jsonWriter = new JSONWriterUTF16JDK8(context);
            jsonWriter.writeString(STR);
            String str = jsonWriter.toString();
            assertEquals(JSON_STR, str);
            assertEquals(STR, JSON.parse(str));
        }
    }

    @Test
    public void testJSONWriterUTF8JDK9() {
        if (STRING_VALUE == null) {
            return;
        }

        for (int i = 0; i < STRINGS.length; i++) {
            String STR = STRINGS[i];
            String JSON_STR = JSON_STRINGS[i];
            JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.EscapeNoneAscii);
            JSONWriter jsonWriter = new JSONWriterUTF8(context);
            jsonWriter.writeString(STR);
            String str = jsonWriter.toString();
            assertEquals(JSON_STR, str);
            assertEquals(STR, JSON.parse(str));
        }
    }

    @Test
    public void testJSONWriterUTF8() {
        for (int i = 0; i < STRINGS.length; i++) {
            String STR = STRINGS[i];
            String JSON_STR = JSON_STRINGS[i];
            JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.EscapeNoneAscii);
            JSONWriter jsonWriter = new JSONWriterUTF8(context);
            jsonWriter.writeString(STR);
            String str = jsonWriter.toString();
            assertEquals(JSON_STR, str);
            assertEquals(STR, JSON.parse(str));
        }
    }
}
