package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

public class Issue1165 {
    @Test
    public void testUTF16() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        for (int i = 0; i < 1_000_000; i++) {
            jsonWriter.writeChar(' ');
        }
        jsonWriter.close();
    }

    @Test
    public void testUTF8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < 1_000_000; i++) {
            jsonWriter.writeChar(' ');
        }
        jsonWriter.close();
    }
}
