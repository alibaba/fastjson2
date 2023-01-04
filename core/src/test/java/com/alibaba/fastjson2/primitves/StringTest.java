package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.String1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTest {
    @Test
    public void test_field_null_default_features_str() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        long features = JSONWriter.Feature.WriteNulls.mask;
        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<String1> objectWriter
                    = creator.createObjectWriter(String1.class, features);

            JSONWriter jsonWriter = JSONWriter.of();
            String1 vo = new String1();
            objectWriter.write(jsonWriter, vo);
            assertEquals("{\"id\":null}",
                    jsonWriter.toString());
        }
    }

    @Test
    public void test_for_emoji() throws Exception {
        String val = "An 😀awesome 😃string with a few 😉emojis!®";

        String1 vo = new String1();
        vo.setId(val);

        {
            String str = JSON.toJSONString(vo);
            String1 o2 = JSON.parseObject(str, String1.class);
            assertEquals(vo.getId(), o2.getId());
        }

        {
            byte[] utf8Bytes = JSON.toJSONBytes(vo);
            String1 o3 = JSON.parseObject(utf8Bytes, String1.class);
            assertEquals(vo.getId(), o3.getId());
        }
    }

    @Test
    public void test_for_special() throws Exception {
        String val = "\"'\\\r\n\f\b\t中国😀😉®";

        String1 vo = new String1();
        vo.setId(val);

        {
            String str = JSON.toJSONString(vo);
            System.out.println(str);
            String1 o2 = JSON.parseObject(str, String1.class);
            assertEquals(vo.getId(), o2.getId());
        }

        {
            byte[] utf8Bytes = JSON.toJSONBytes(vo);
            System.out.println(new String(utf8Bytes));
            String1 o3 = JSON.parseObject(utf8Bytes, String1.class);
            assertEquals(vo.getId(), o3.getId());
        }
    }

    @Test
    public void test_for_u() throws Exception {
        char ch = '中';
        String str = "{\"id\":\"\\u" + Integer.toHexString(ch) + "\"}";
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            String1 o2 = JSON.parseObject(str, String1.class);
            assertEquals(Character.toString(ch), o2.getId());
        }

        {
            String1 o3 = JSON.parseObject(utf8Bytes, String1.class);
            assertEquals(Character.toString(ch), o3.getId());
        }
    }

    @Test
    public void test_for_u_1() throws Exception {
        char ch = '中';
        String str = "{\"id\":\"\\\"\\u" + Integer.toHexString(ch) + "\"}";
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            String1 o2 = JSON.parseObject(str, String1.class);
            assertEquals("\"" + ch, o2.getId());
        }

        {
            String1 o3 = JSON.parseObject(utf8Bytes, String1.class);
            assertEquals("\"" + ch, o3.getId());
        }
    }

    @Test
    public void test_for_x() throws Exception {
        char ch = 'A';
        String str = "{\"id\":\"\\\"\\x" + Integer.toHexString(ch) + "\"}";
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            String1 o2 = JSON.parseObject(str, String1.class);
            assertEquals("\"" + ch, o2.getId());
        }

        {
            String1 o3 = JSON.parseObject(utf8Bytes, String1.class);
            assertEquals("\"" + ch, o3.getId());
        }
    }

    @Test
    public void test_null() throws Exception {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            FieldWriter fieldWriter = creator.createFieldWriter(String1.class, "id", 0, 0, null, String1.class.getMethod("getId"));
            ObjectWriter<String1> objectWriter
                    = creator.createObjectWriter(fieldWriter);

            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                String1 vo = new String1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("[null]",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                String1 vo = new String1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{}",
                        jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.WriteNulls);
                String1 vo = new String1();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"id\":null}",
                        jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_utf8JSONB() throws Exception {
        String str = "0123456789ABC中国 😀😉 ×";
        byte[] bytes = JSONB.toBytes(str, StandardCharsets.UTF_8);

        byte[] valueBytes = new byte[bytes.length * 2];
        int utf16_len = IOUtils.decodeUTF8(bytes, 2, bytes.length - 2, valueBytes);
        String str_utf16be = new String(valueBytes, 0, utf16_len, StandardCharsets.UTF_16LE);
        assertEquals(str, str_utf16be);
    }

    @Test
    public void getObjectClass() {
        assertEquals(
                String.class,
                JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getObjectReader(String.class)
                        .getObjectClass()
        );
    }
}
