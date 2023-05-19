package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1497 {
    @Test
    public void testUTF8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeDateYYYMMDD10(1921, 12, 13);
        assertEquals("\"1921-12-13\"", jsonWriter.toString());
        jsonWriter.close();
    }

    @Test
    public void testUTF16() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter.writeDateYYYMMDD10(1921, 12, 13);
        assertEquals("\"1921-12-13\"", jsonWriter.toString());
        jsonWriter.close();
    }
}
