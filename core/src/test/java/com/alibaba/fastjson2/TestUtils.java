package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;

import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static final boolean GRAALVM = false;
    public static final boolean ANDROID = false;

    public static ObjectReaderCreator READER_CREATOR = ObjectReaderCreator.INSTANCE;
    public static ObjectWriterCreator WRITER_CREATOR = ObjectWriterCreator.INSTANCE;

    public static ObjectReaderCreator[] readerCreators() {
        return new ObjectReaderCreator[]{ObjectReaderCreator.INSTANCE};
    }

    public static ObjectWriterCreator[] writerCreators() {
        return new ObjectWriterCreator[]{ObjectWriterCreator.INSTANCE};
    }

    public static ObjectReader createObjectReaderLambda(Class<?> clazz) {
        return null;
    }

    public static ObjectWriter createObjectWriterLambda(Class<?> clazz) {
        return null;
    }

    public static String encodeToBase64(byte[] bytes) {
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    public static JSONReader[] createJSONReaders(String json) {
        return new JSONReader[]{JSONReader.of(json)};
    }

    public static JSONReader[] createJSONReaderStr(String json) {
        return new JSONReader[]{JSONReader.of(json)};
    }

    public static void check(Object o, String jsonStr, String... ignore) {
        String result = JSON.toJSONString(o);
        JSONObject parsed = JSON.parseObject(result);
        JSONObject expected = JSON.parseObject(jsonStr);
        if (!parsed.equals(expected)) {
            throw new AssertionError("Expected " + jsonStr + " but got " + result);
        }
    }

    public static boolean isSameJSONObject(JSONObject a, JSONObject b) {
        return a.toString().equals(b.toString());
    }

    public static byte[] toBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
