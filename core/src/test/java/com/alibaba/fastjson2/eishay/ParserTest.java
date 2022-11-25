package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.eishay.vo.Image;
import com.alibaba.fastjson2.eishay.vo.Media;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
    ObjectReader<MediaContent> mediaContentConsumer;

    public ParserTest() {
        JSONReader p = null;
        final ObjectReader<Media> mediaConsumer = ObjectReaders.of(
                Media::new,
                fieldReaderInt("bitrate", Media::setBitrate),
                fieldReaderLong("duration", Media::setDuration),
                fieldReader("format", String.class, Media::setFormat),
                fieldReaderInt("height", Media::setHeight),
                fieldReaderList("persons", String.class, ArrayList::new, Media::setPersons, null),
                fieldReader("player", Media.Player.class, Media::setPlayer),
                fieldReaderLong("size", Media::setSize),
                fieldReader("title", String.class, Media::setTitle),
                fieldReader("uri", String.class, Media::setUri),
                fieldReaderInt("width", Media::setWidth)
        );

        final ObjectReader<Image> imageConsumer = ObjectReaders.of(
                Image::new,
                fieldReaderInt("height", Image::setHeight),
                fieldReader("size", Image.Size.class, Image::setSize),
                fieldReader("title", String.class, Image::setTitle),
                fieldReader("uri", String.class, Image::setUri),
                fieldReaderInt("width", Image::setWidth)
        );

        mediaContentConsumer = ObjectReaders.of(
                MediaContent::new,
                fieldReader("media", Media.class, MediaContent::setMedia, mediaConsumer),
                fieldReaderList("images", Image.class, ArrayList::new, MediaContent::setImages, imageConsumer)
        );
    }

//    @Test
//    public void test_0_utf8() {
//
//        JSONReader jr = JSONReader.of(str.getBytes(StandardCharsets.UTF_8)); //
//        mediaContentConsumer.readObject(jr, null);
//
//        JSONWriter jw = JSONWriter.of();
//
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        ObjectWriter<MediaContent> ow = of(MediaContent.class);
//    }

    @Test
    public void test_1_utf8() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jsonReader = JSONReader.of(utf8Bytes);
        MediaContent o = jsonReader.read(MediaContent.class);

//
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeAny(o);
//        System.out.println(jsonWriter);
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        JSONSerializer<MediaContent> js = JSONReader.of(MediaContent.class);
//        js.wr
    }

    @Test
    public void test_beanToArray_utf8() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jsonReader = JSONReader.of(utf8Bytes);
        MediaContent o = jsonReader.read(MediaContent.class);

//
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.config(JSONWriter.Feature.BeanToArray);
        jsonWriter.writeAny(o);
        System.out.println(jsonWriter);
        String arrayStr = jsonWriter.toString();
        Object o2 = JSON.parseObject(arrayStr, MediaContent.class, JSONReader.Feature.SupportArrayToBean);
//        js.wr
    }

    @Test
    public void test_1_jsonb() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jr = JSONReader.of(utf8Bytes);
        MediaContent o = jr.read(MediaContent.class);

        JSONWriter jw = JSONWriter.ofJSONB();
        jw.config(JSONWriter.Feature.FieldBased, JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.WriteNameAsSymbol);
        jw.writeAny(o);
//        System.out.println(jw);

        byte[] jsonbBytes = jw.getBytes();
        assertEquals("{\n" +
                "\t\"images#0\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"height#1\":768,\n" +
                "\t\t\t\"size#2\":1,\n" +
                "\t\t\t\"title#3\":\"Javaone Keynote\",\n" +
                "\t\t\t\"uri#4\":\"http://javaone.com/keynote_large.jpg\",\n" +
                "\t\t\t\"width#5\":1024\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"#1\":240,\n" +
                "\t\t\t\"#2\":0,\n" +
                "\t\t\t\"#3\":\"Javaone Keynote\",\n" +
                "\t\t\t\"#4\":\"http://javaone.com/keynote_small.jpg\",\n" +
                "\t\t\t\"#5\":320\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"media#6\":{\n" +
                "\t\t\"bitrate#7\":262144,\n" +
                "\t\t\"duration#8\":18000000,\n" +
                "\t\t\"format#9\":\"video/mpg4\",\n" +
                "\t\t\"#1\":480,\n" +
                "\t\t\"persons#10\":[\n" +
                "\t\t\t\"Bill Gates\",\n" +
                "\t\t\t\"Steve Jobs\"\n" +
                "\t\t],\n" +
                "\t\t\"player#11\":0,\n" +
                "\t\t\"#2\":58982400,\n" +
                "\t\t\"#3\":\"Javaone Keynote\",\n" +
                "\t\t\"#4\":\"http://javaone.com/keynote.mpg\",\n" +
                "\t\t\"#5\":640\n" +
                "\t}\n" +
                "}", new JSONBDump(jsonbBytes, true).toString());

//        Object jsonb = JSONB.readFrom(jsonbBytes);

        assertEquals(350, jsonbBytes.length);
        // 331 360 348 352 350

        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
        MediaContent o1 = jsonReader.read(MediaContent.class);
        assertEquals(o.getMedia().getFormat(), o1.getMedia().getFormat());
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        JSONSerializer<MediaContent> js = JSONReader.of(MediaContent.class);
//        js.wr
    }

    @Test
    public void test_1_jsonb_2() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jr = JSONReader.of(utf8Bytes);
        MediaContent o = jr.read(MediaContent.class);

        byte[] jsonbBytes = JSONB
                .toBytes(
                        o,
                        JSONWriter.Feature.ReferenceDetection,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                        JSONWriter.Feature.WriteNulls,
                        JSONWriter.Feature.NotWriteDefaultValue,
                        JSONWriter.Feature.FieldBased,
                        JSONWriter.Feature.IgnoreErrorGetter,
                        JSONWriter.Feature.WriteNameAsSymbol
                );
//        System.out.println(jw);

        JSONBDump.dump(jsonbBytes);

//        Object jsonb = JSONB.readFrom(jsonbBytes);

        assertEquals(397, jsonbBytes.length);
        // 331 360 409 396 395 399 397

        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
        MediaContent o1 = jsonReader.read(MediaContent.class);
        assertEquals(o.getMedia().getFormat(), o1.getMedia().getFormat());
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        JSONSerializer<MediaContent> js = JSONReader.of(MediaContent.class);
//        js.wr
    }

    @Test
    public void test_1_jsonb_arrayMapping() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jr = JSONReader.of(utf8Bytes);
        MediaContent o = jr.read(MediaContent.class);

        JSONWriter jw = JSONWriter.ofJSONB();
        jw.config(JSONWriter.Feature.BeanToArray);
        jw.writeAny(o);
//        System.out.println(jw);

        byte[] jsonbBytes = jw.getBytes();
        JSONBDump.dump(jsonbBytes);

//        Object jsonb = JSONB.readFrom(jsonbBytes);

        assertEquals(223, jsonbBytes.length);

        JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length);
        jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
        MediaContent o1 = jsonReader.read(MediaContent.class);
        assertEquals(o.getMedia().getFormat(), o1.getMedia().getFormat());
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        JSONSerializer<MediaContent> js = JSONReader.of(MediaContent.class);
//        js.wr
    }

    @Test
    public void test_1_jsonb_symbolTable() {
        byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8);

        JSONReader jr = JSONReader.of(utf8Bytes);
        MediaContent o = jr.read(MediaContent.class);

        SymbolTable symbolTable = JSONB.symbolTable(
                "images",
                "height",
                "size",
                "title",
                "uri",
                "width",
                "media",
                "bitrate",
                "duration",
                "format",
                "persons",
                "player",
                "LARGE",
                "SMALL",
                "JAVA",
                "FLASH",
                "copyright"
        );

        JSONWriter jsonWriter = JSONWriter.ofJSONB(symbolTable);
        jsonWriter.writeAny(o);
//        System.out.println(jsonWriter);

        byte[] jsonbBytes = jsonWriter.getBytes();
        JSONBDump.dump(jsonbBytes, symbolTable);

        assertEquals(276, jsonbBytes.length);
        // 260 273 277 276

        JSONReader jsonReader1 = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length, symbolTable);
        Object jsonb = jsonReader1.readAny();

        JSONReader jsonReader2 = JSONReader.ofJSONB(jsonbBytes, 0, jsonbBytes.length, symbolTable);
        MediaContent o1 = jsonReader2.read(MediaContent.class);
//        ObjectWriterContext ctx = JSONFactory.createWriteContext();
//        JSONSerializer<MediaContent> js = JSONReader.of(MediaContent.class);
//        js.wr
    }

    static final String str = "{\"images\":\n" +
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
}
