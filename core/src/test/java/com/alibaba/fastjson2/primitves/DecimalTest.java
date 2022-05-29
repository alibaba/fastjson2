package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.BigDecimal1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalTest {
    @Test
    public void test_field_null_str() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BigDecimal1> objectWriter = creator.createObjectWriter(BigDecimal1.class);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                BigDecimal1 vo = new BigDecimal1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                BigDecimal1 vo = new BigDecimal1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_field_null_default_features_str() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        long features = JSONWriter.Feature.WriteNulls.mask;
        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<BigDecimal1> objectWriter
                    = creator.createObjectWriter(BigDecimal1.class, features);

            JSONWriter jsonWriter = JSONWriter.of();
            BigDecimal1 vo = new BigDecimal1();
            objectWriter.write(jsonWriter, vo);
            assertEquals("{\"id\":null}",
                    jsonWriter.toString());
        }
    }

    @Test
    public void test_null_str() {
        BigDecimal decimal = null;
        assertEquals("null",
                JSON.toJSONString(decimal));
    }

    @Test
    public void test_null_utf8() {
        BigDecimal decimal = null;
        assertEquals("null",
                new String(
                        JSON.toJSONBytes(decimal)));
    }

    @Test
    public void test_BrowserCompatible_str() {
        BigDecimal decimal = new BigDecimal("90071992547409910");
        assertEquals("\"90071992547409910\"",
                JSON.toJSONString(decimal, JSONWriter.Feature.BrowserCompatible));
    }

    @Test
    public void test_BrowserCompatible_utf8() {
        BigDecimal decimal = new BigDecimal("90071992547409910");
        assertEquals("\"90071992547409910\"",
                new String(
                        JSON.toJSONBytes(decimal, JSONWriter.Feature.BrowserCompatible)));
    }
}
