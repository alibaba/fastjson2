package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterPrettyTest {
    @Test
    public void writeDateTime19() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
        jsonWriter.writeDateTime19(2018, 7, 5, 12, 13, 14);
        assertEquals("\"2018-07-05 12:13:14\"", jsonWriter.toString());
    }

    @Test
    public void writeDateTimeISO8601() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
        jsonWriter.writeDateTimeISO8601(2018, 7, 5, 12, 13, 14, 0, 0, true);
        assertEquals("\"2018-07-05T12:13:14Z\"", jsonWriter.toString());
    }

    @Test
    public void writeDateYYYMMDD10() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
        jsonWriter.writeDateYYYMMDD10(2018, 7, 5);
        assertEquals("\"2018-07-05\"", jsonWriter.toString());
    }

    @Test
    public void writeTimeHHMMSS8() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
        jsonWriter.writeTimeHHMMSS8(12, 13, 14);
        assertEquals("\"12:13:14\"", jsonWriter.toString());
    }

    @Test
    public void writeRaw() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeRaw(new char[] {'A'});
        assertEquals("A", jsonWriter.toString());
    }

    @Test
    public void writeRaw1() {
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeRaw(new byte[] {'A'});
        assertEquals("A", jsonWriter.toString());
    }

    @Test
    public void writeRaw2() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeNameRaw(new char[] {'A'}, 0, 1);
        assertEquals(",A", jsonWriter.toString());
    }

    @Test
    public void writeRaw3() throws Exception {
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.startObject();
        jsonWriter.writeNameRaw(new byte[] {'A'}, 0, 1);
        jsonWriter.writeColon();
        jsonWriter.writeInt32(1);
        jsonWriter.endObject();
        assertEquals("{\n" +
                "\tA:1\n" +
                "}", jsonWriter.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonWriter.flushTo(out, StandardCharsets.UTF_8);
        assertEquals("{\n" +
                "\tA:1\n" +
                "}", new String(out.toByteArray()));
    }

    @Test
    public void writeInt16() throws Exception {
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeInt16(new short[]{1, 2});
        assertEquals("[\n" +
                "\t1,\n" +
                "\t2\n" +
                "]", jsonWriter.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonWriter.flushTo(out);
        assertEquals("[\n" +
                "\t1,\n" +
                "\t2\n" +
                "]", new String(out.toByteArray()));
    }

    @Test
    public void writeRaw4() {
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeRaw('A');
        assertEquals("A", jsonWriter.toString());
    }

    @Test
    public void test_base64() {
        byte[] bytes = new byte[1024];
        new Random().nextBytes(bytes);
        {
            JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
            jsonWriter.writeBase64(bytes);
            String str = jsonWriter.toString();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            assertEquals(base64, str.substring(1, str.length() - 1));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.PrettyFormat);
            jsonWriter.writeBase64(bytes);
            String str = jsonWriter.toString();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            assertEquals(base64, str.substring(1, str.length() - 1));
        }
    }
}
