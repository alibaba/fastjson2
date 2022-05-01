package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParserTest_media {
    String str = "{\"images\":\n" +
            "\t[\n" +
            "\t\t{\n" +
            "\t\t\t\"height\":768,\n" +
            "\t\t\t\"size\":\"LARGE\",\n" +
            "\t\t\t\"title\":\"Javaone Keynote\",\n" +
            "\t\t\t\"uri\":\"http://javaone.com/keynote_large.jpg\",\n" +
            "\t\t\t\"width\":1024\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"height\":240,\n" +
            "\t\t\t\"size\":\"SMALL\",\n" +
            "\t\t\t\"title\":\"Javaone Keynote\",\n" +
            "\t\t\t\"uri\":\"http://javaone.com/keynote_small.jpg\",\n" +
            "\t\t\t\"width\":320\n" +
            "\t\t}\n" +
            "\t],\n" +
            "\t\"media\":\n" +
            "\t{\n" +
            "\t\t\"bitrate\":262144,\n" +
            "\t\t\"duration\":18000000,\n" +
            "\t\t\"format\":\"video/mpg4\",\n" +
            "\t\t\"height\":480,\n" +
            "\t\t\"persons\":\n" +
            "\t\t\t[\n" +
            "\t\t\t\t\"Bill Gates\",\n" +
            "\t\t\t\t\"Steve Jobs\"\n" +
            "\t\t\t],\n" +
            "\t\t\"player\":\"JAVA\",\n" +
            "\t\t\"size\":58982400,\n" +
            "\t\t\"title\":\"Javaone Keynote\",\n" +
            "\t\t\"uri\":\"http://javaone.com/keynote.mpg\",\n" +
            "\t\t\"width\":640\n" +
            "\t}\n" +
            "}";

    @Test
    public void test_0_ascii_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf8() {
        JSONReader lexer = JSONReader.of(str.getBytes(StandardCharsets.UTF_8));
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16() {
        JSONReader lexer = JSONReader.of(str.toCharArray());
        lexerTest(lexer);
    }

    @Test
    public void test_0_string() {
        JSONReader lexer = JSONReader.of(str);
        lexerTest(lexer);
    }

    @Test
    public void test_0_stringExtract() throws Exception {
        JSONReader.Context ctx = JSONFactory.createReadContext();
        Class<?> clazz = Class.forName("com.alibaba.fastjson2.JSONReaderStr");
        Constructor<?> constructor = clazz.getDeclaredConstructor(JSONReader.Context.class, String.class, int.class, int.class);
        constructor.setAccessible(true);
        JSONReader lexer = (JSONReader) constructor.newInstance(ctx, str, 0, str.length());
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_bytes() {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_16);
        JSONReader lexer = JSONReader.of(bytes, 0, bytes.length, StandardCharsets.UTF_16);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf8_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_is() {
        JSONReader lexer = JSONReader.of(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_16)), StandardCharsets.UTF_16);
        lexerTest(lexer);
    }

    @Test
    public void test_0_utf16_reader() {
        JSONReader lexer = JSONReader.of(new StringReader(str));
        lexerTest(lexer);
    }

    void lexerTest(JSONReader lexer) {
        Map map = lexer.readObject();

        List images = (List) map.get("images");
        assertNotNull(images);
        assertEquals(2, images.size());

        Map image_0 = (Map) images.get(0);
        assertEquals(5, image_0.size());
        assertEquals(Integer.valueOf(768), image_0.get("height"));
        assertEquals("LARGE", image_0.get("size"));
        assertEquals("Javaone Keynote", image_0.get("title"));
        assertEquals("http://javaone.com/keynote_large.jpg", image_0.get("uri"));
        assertEquals(Integer.valueOf(1024), image_0.get("width"));

        Map image_1 = (Map) images.get(1);
        assertEquals(5, image_0.size());
        assertEquals(Integer.valueOf(240), image_1.get("height"));
        assertEquals("SMALL", image_1.get("size"));
        assertEquals("Javaone Keynote", image_1.get("title"));
        assertEquals("http://javaone.com/keynote_small.jpg", image_1.get("uri"));
        assertEquals(Integer.valueOf(320), image_1.get("width"));

        Map media = (Map) map.get("media");
        assertEquals(10, media.size());
        assertEquals(Integer.valueOf(262144), media.get("bitrate"));
        assertEquals(Integer.valueOf(18000000), media.get("duration"));
        assertEquals("video/mpg4", media.get("format"));
        assertEquals(Integer.valueOf(480), media.get("height"));

        List persons = (List) media.get("persons");
        assertEquals(2, persons.size());
        assertEquals("Bill Gates", persons.get(0));
        assertEquals("Steve Jobs", persons.get(1));

        assertEquals("JAVA", media.get("player"));
        assertEquals(Integer.valueOf(58982400), media.get("size"));
        assertEquals("Javaone Keynote", media.get("title"));
        assertEquals("http://javaone.com/keynote.mpg", media.get("uri"));
        assertEquals(Integer.valueOf(640), media.get("width"));
    }
}
