package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONWriter.PRETTY_2_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterWriteNameRawTest {
    @Test
    public void test() {
        long v = 3978425819141910881L;

        String expected = ",\n" +
                "\"a1234567\":";
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            jsonWriter.pretty = PRETTY_2_SPACE;
            jsonWriter.startObject = false;

            jsonWriter.writeName8Raw(v);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriterUTF8 jsonWriter = (JSONWriterUTF8) JSONWriter.ofUTF8();
            jsonWriter.bytes = new byte[0];
            jsonWriter.pretty = PRETTY_2_SPACE;
            jsonWriter.startObject = false;

            jsonWriter.writeName8Raw(v);
            assertEquals(expected, jsonWriter.toString());
        }
    }
}
