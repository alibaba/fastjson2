package com.alibaba.fastjson2.example.graalvm_native;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.example.graalvm_native.vo.Image;
import com.alibaba.fastjson2.example.graalvm_native.vo.Media;
import com.alibaba.fastjson2.example.graalvm_native.vo.MediaContent;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.util.ArrayList;

import static com.alibaba.fastjson2.reader.ObjectReaders.*;
import static com.alibaba.fastjson2.reader.ObjectReaders.fieldReaderString;

public class App {
    public static void main(String[] args) throws Exception {
        String str = "{\"images\": [{\n" +
                "      \"height\":768,\n" +
                "      \"size\":\"LARGE\",\n" +
                "      \"title\":\"Javaone Keynote\",\n" +
                "      \"uri\":\"http://javaone.com/keynote_large.jpg\",\n" +
                "      \"width\":1024\n" +
                "    }, {\n" +
                "      \"height\":240,\n" +
                "      \"size\":\"SMALL\",\n" +
                "      \"title\":\"Javaone Keynote\",\n" +
                "      \"uri\":\"http://javaone.com/keynote_small.jpg\",\n" +
                "      \"width\":320\n" +
                "    }\n" +
                "  ],\n" +
                "  \"media\": {\n" +
                "    \"bitrate\":262144,\n" +
                "    \"duration\":18000000,\n" +
                "    \"format\":\"video/mpg4\",\n" +
                "    \"height\":480,\n" +
                "    \"persons\": [\n" +
                "      \"Bill Gates\",\n" +
                "      \"Steve Jobs\"\n" +
                "    ],\n" +
                "    \"player\":\"JAVA\",\n" +
                "    \"size\":58982400,\n" +
                "    \"title\":\"Javaone Keynote\",\n" +
                "    \"uri\":\"http://javaone.com/keynote.mpg\",\n" +
                "    \"width\":640\n" +
                "  }\n" +
                "}";

        registerWriter();
        registerReader();

//        com.alibaba.fastjson2.reader.ObjectReaderProvider provider = com.alibaba.fastjson2.JSONFactory.getDefaultObjectReaderProvider();
//        provider.register(MediaContent.class, new com.alibaba.fastjson2.example.graalvm_native.vo.MediaContent_FASTJSONReader());
//        provider.register(Media.class, new com.alibaba.fastjson2.example.graalvm_native.vo.Media_FASTJSONReader());
//        provider.register(Image.class, new com.alibaba.fastjson2.example.graalvm_native.vo.Image_FASTJSONReader());

        MediaContent mediaContent = JSON.parseObject(str, MediaContent.class);

        int LOOP_COUNT = 1000000;
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSON.toJSONString(mediaContent);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 eishay toJSONString time : " + millis);
        }
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSON.parseObject(str, MediaContent.class);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 eishay parseObject time : " + millis);
        }
    }

    private static void registerWriter() {
        JSON.register(MediaContent.class, ObjectWriters.objectWriter(
                MediaContent.class,
                ObjectWriters.fieldWriter("media", Media.class, MediaContent::getMedia),
                ObjectWriters.fieldWriterList("images", Image.class, MediaContent::getImages)
        ));

        JSON.register(Media.class, ObjectWriters.objectWriter(
                Media.class,
                ObjectWriters.fieldWriter("bitrate", Media::getBitrate),
                ObjectWriters.fieldWriter("duration", Media::getDuration),
                ObjectWriters.fieldWriter("format", Media::getFormat),
                ObjectWriters.fieldWriter("height", Media::getHeight),
                ObjectWriters.fieldWriterList("persons", String.class, Media::getPersons),
                ObjectWriters.fieldWriter("player", Media.Player.class, Media::getPlayer),
                ObjectWriters.fieldWriter("size", Media::getSize),
                ObjectWriters.fieldWriter("title", Media::getTitle),
                ObjectWriters.fieldWriter("uri", Media::getUri),
                ObjectWriters.fieldWriter("width", Media::getWidth),
                ObjectWriters.fieldWriter("copyright", Media::getCopyright)
        ));

        JSON.register(Image.class, ObjectWriters.objectWriter(
                Image.class,
                ObjectWriters.fieldWriter("height", Image::getHeight),
                ObjectWriters.fieldWriter("size", Image.Size.class, Image::getSize),
                ObjectWriters.fieldWriter("title", Image::getTitle),
                ObjectWriters.fieldWriter("uri", Image::getUri),
                ObjectWriters.fieldWriter("width", Image::getWidth)
        ));
    }

    private static void registerReader() {
        JSON.register(MediaContent.class, ObjectReaders.of(
                MediaContent::new,
                fieldReader("media", Media.class, MediaContent::setMedia),
                fieldReaderList("images", Image.class, ArrayList::new, MediaContent::setImages)
        ));

        JSON.register(Media.class, ObjectReaders.of(
                Media::new,
                fieldReaderInt("bitrate", Media::setBitrate),
                fieldReaderLong("duration", Media::setDuration),
                fieldReaderString("format", Media::setFormat),
                fieldReaderInt("height", Media::setHeight),
                fieldReaderList("persons", String.class, ArrayList::new, Media::setPersons),
                fieldReader("player", Media.Player.class, Media::setPlayer),
                fieldReaderLong("size", Media::setSize),
                fieldReaderString("title", Media::setTitle),
                fieldReaderString("uri", Media::setUri),
                fieldReaderInt("width", Media::setWidth),
                fieldReaderString("copyright", Media::setCopyright)
        ));

        JSON.register(Image.class, ObjectReaders.of(
                Image::new,
                fieldReaderInt("height", Image::setHeight),
                fieldReader("size", Image.Size.class, Image::setSize),
                fieldReaderString("title", Image::setTitle),
                fieldReaderString("uri", Image::setUri),
                fieldReaderInt("width", Image::setWidth)
        ));
    }
}
