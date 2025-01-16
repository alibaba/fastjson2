package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class Issue2998 {
    private final String[] binaryArray = {
            "11111100", "00010100", "11100001", "11000100", "00100110",
    };

    private byte[] getByteArray() {
        byte[] byteArray = new byte[binaryArray.length];
        for (int i = 0; i < binaryArray.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(binaryArray[i], 2);
        }
        return byteArray;
    }

    private final byte[] byteArray = getByteArray();

    // 1. parseObject(byte[], int, int, Charset, Type, Feature...)
    @Test
    public void testParseObject_Charset() {
        try {
            JSON.parseObject(byteArray, 0, byteArray.length, StandardCharsets.UTF_8, Object.class, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    @Test
    public void testParseObject_byteArray_Type_Features() {
        try {
            Type type = JSONObject.class;
            JSON.parseObject(byteArray, type, Feature.SupportAutoType);
        } catch (JSONException e) {
        }
    }

    // 2. parseObject(byte[], int, int, CharsetDecoder, Type, Feature...)
    @Test
    public void testParseObject_CharsetDecoder() {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        try {
            JSON.parseObject(byteArray, 0, byteArray.length, decoder, Object.class, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 3. parseObject(byte[], Type, SerializeFilter, Feature...)
    @Test
    public void testParseObject_SerializeFilter() {
        SerializeFilter filter = null;
        try {
            JSON.parseObject(byteArray, Object.class, filter, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 4. parseObject(byte[], Type, JSONReader.Context)
    @Test
    public void testParseObject_JSONReaderContext() {
        JSONReader.Context context = null; // Customize context if needed
        try {
            JSON.parseObject(byteArray, Object.class, context);
        } catch (JSONException e) {
        }
    }

    // 5. parseObject(byte[], Type, Feature...)
    @Test
    public void testParseObject_Type_Feature() {
        try {
            JSON.parseObject(byteArray, Object.class, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 6. parseObject(byte[], Class<T>, Feature...)
    @Test
    public void testParseObject_Class_Feature() {
        try {
            JSON.parseObject(byteArray, Object.class, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 7. parseObject(byte[], Feature...)
    @Test
    public void testParseObject_JSONObject() {
        try {
            JSON.parseObject(byteArray, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 8. parseObject(byte[], int, int, Charset, Type, ParserConfig, ParseProcess, int, Feature...)
    @Test
    public void testParseObject_ParserConfig_ParseProcess() {
        ParserConfig config = ParserConfig.getGlobalInstance();
        ParseProcess processor = null; // Customize processor if needed
        try {
            JSON.parseObject(byteArray, 0, byteArray.length, StandardCharsets.UTF_8, Object.class, config, processor, 0, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 9. parseObject(byte[], Charset, Type, ParserConfig, ParseProcess, int, Feature...)
    @Test
    public void testParseObject_Charset_Config() {
        ParserConfig config = ParserConfig.getGlobalInstance();
        ParseProcess processor = null;
        try {
            JSON.parseObject(byteArray, StandardCharsets.UTF_8, Object.class, config, processor, 0, Feature.AllowComment);
        } catch (JSONException e) {
        }
    }

    // 10. parseObject(InputStream, Type, Feature...) throws IOException
    @Test
    public void testParseObject_InputStream_Type() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            JSON.parseObject(inputStream, Object.class, Feature.AllowComment);
        }
    }

    // 11. parseObject(InputStream, Class<T>, Feature...) throws IOException
    @Test
    public void testParseObject_InputStream_Class() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            JSON.parseObject(inputStream, Object.class, Feature.AllowComment);
        }
    }

    // 12. parseObject(InputStream, Charset, Type, ParserConfig, ParseProcess, int, Feature...) throws IOException
    @Test
    public void testParseObject_InputStream_Charset_Config() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            ParserConfig config = ParserConfig.getGlobalInstance();
            ParseProcess processor = null;
            JSON.parseObject(inputStream, StandardCharsets.UTF_8, Object.class, config, processor, 0, Feature.AllowComment);
        }
    }

    // 13. parseObject(InputStream, Charset, Type, ParserConfig, Feature...) throws IOException
    @Test
    public void testParseObject_InputStream_Charset() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            ParserConfig config = ParserConfig.getGlobalInstance();
            JSON.parseObject(inputStream, StandardCharsets.UTF_8, Object.class, config, Feature.AllowComment);
        }
    }

    // 14. parseObject(InputStream, Charset, Type, Feature...) throws IOException
    @Test
    public void testParseObject_InputStream_Charset_Feature() throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            JSON.parseObject(inputStream, StandardCharsets.UTF_8, Object.class, Feature.AllowComment);
        }
    }
}
