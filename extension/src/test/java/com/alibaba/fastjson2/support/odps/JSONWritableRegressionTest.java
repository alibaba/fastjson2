package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSON;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.WritableUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONWritableRegressionTest {
    @Test
    public void repeatedExtraction() {
        JSONExtractScalar extractor = new JSONExtractScalar("$.value");
        String[] values = {"\"first\"", "true", "\"second\"", "null", "\"third\"", "123", "\"fourth\"", "1234567890123", "\"fifth\"", "\"escaped\\nvalue\""};
        for (String value : values) {
            Object expected = JSON.parse(value);
            assertEquals(String.valueOf(expected), extractor.eval(new Text("{\"value\":" + value + "}")).toString());
        }
    }

    @Test
    public void integerExtremes() {
        JSONExtractScalar extractor = new JSONExtractScalar("$.value");
        JSONExtractScalar.ExtractValueConsumer consumer = extractor.valueConsumer;
        consumer.accept(Integer.MIN_VALUE);
        assertEquals(Integer.toString(Integer.MIN_VALUE), extractor.text.toString());
        consumer.accept(Long.MIN_VALUE);
        assertEquals(Long.toString(Long.MIN_VALUE), extractor.text.toString());
        consumer.accept((Number) Integer.MIN_VALUE);
        assertEquals(Integer.toString(Integer.MIN_VALUE), extractor.text.toString());
        consumer.accept((Number) Long.MIN_VALUE);
        assertEquals(Long.toString(Long.MIN_VALUE), extractor.text.toString());
    }

    @Test
    public void escapedUnicodeString() {
        JSONExtract extractor = new JSONExtract("$.value");
        String value = "é中文\"\\\n";
        extractor.valueConsumer.accept(value);
        assertEquals(JSON.toJSONString(value), extractor.text.toString());
    }

    @Test
    public void overwriteBorrowedSlice() throws Exception {
        JSONWritable writable = new JSONWritable("prefixvalue".getBytes(StandardCharsets.UTF_8));
        writable.off = 6;
        writable.length = 5;
        writable.setCapacity(20, true);
        assertEquals("value", writable.toString());

        writable.off = 1;
        writable.length = 4;
        writable.setCapacity(20, true);
        assertEquals("alue", writable.toString());

        writable.off = 1;
        writable.set("replacement");
        assertEquals("replacement", writable.toString());

        writable.off = 1;
        writable.set("other".getBytes(StandardCharsets.UTF_8));
        assertEquals("other", writable.toString());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new JSONWritable("read".getBytes(StandardCharsets.UTF_8)).write(new DataOutputStream(bytes));
        writable.off = 1;
        writable.readFields(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
        assertEquals("read", writable.toString());
    }

    @Test
    public void negativeSerializedLength() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        WritableUtils.writeVInt(new DataOutputStream(bytes), -1);
        JSONWritable writable = new JSONWritable();
        assertThrows(java.io.IOException.class,
                () -> writable.readFields(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))));
    }
}
