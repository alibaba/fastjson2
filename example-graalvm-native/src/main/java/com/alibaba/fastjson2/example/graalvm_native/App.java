package com.alibaba.fastjson2.example.graalvm_native;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.util.ArrayList;
import java.util.List;

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

        registerReaderAndWriter();

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

    private static void registerReaderAndWriter() {
        JSON.register(MediaContent.class, ObjectWriters.objectWriter(
                MediaContent.class,
                ObjectWriters.fieldWriter("media", Media.class, MediaContent::getMedia),
                ObjectWriters.fieldWriterList("images", Image.class, MediaContent::getImages)
        ));

        JSON.register(MediaContent.class, ObjectReaders.of(
                MediaContent::new,
                fieldReader("media", Media.class, MediaContent::setMedia),
                fieldReaderList("images", Image.class, ArrayList::new, MediaContent::setImages)
        ));

        JSON.register(Media.class, ObjectWriters.objectWriter(
                Media.class,
                ObjectWriters.fieldWriter("bitrate", Media::getBitrate),
                ObjectWriters.fieldWriter("duration", Media::getDuration),
                ObjectWriters.fieldWriter("format", Media::getFormat),
                ObjectWriters.fieldWriter("height", Media::getHeight),
                ObjectWriters.fieldWriterList("persons", String.class, Media::getPersons),
                ObjectWriters.fieldWriter("player", Player.class, Media::getPlayer),
                ObjectWriters.fieldWriter("size", Media::getSize),
                ObjectWriters.fieldWriter("title", Media::getTitle),
                ObjectWriters.fieldWriter("uri", Media::getUri),
                ObjectWriters.fieldWriter("width", Media::getWidth),
                ObjectWriters.fieldWriter("copyright", Media::getCopyright)
        ));

        JSON.register(Media.class, ObjectReaders.of(
                Media::new,
                fieldReaderInt("bitrate", Media::setBitrate),
                fieldReaderLong("duration", Media::setDuration),
                fieldReaderString("format", Media::setFormat),
                fieldReaderInt("height", Media::setHeight),
                fieldReaderList("persons", String.class, ArrayList::new, Media::setPersons),
                fieldReader("player", Player.class, Media::setPlayer),
                fieldReaderLong("size", Media::setSize),
                fieldReaderString("title", Media::setTitle),
                fieldReaderString("uri", Media::setUri),
                fieldReaderInt("width", Media::setWidth),
                fieldReaderString("copyright", Media::setCopyright)
        ));

        JSON.register(Image.class, ObjectWriters.objectWriter(
                Image.class,
                ObjectWriters.fieldWriter("height", Image::getHeight),
                ObjectWriters.fieldWriter("size", Size.class, Image::getSize),
                ObjectWriters.fieldWriter("title", Image::getTitle),
                ObjectWriters.fieldWriter("uri", Image::getUri),
                ObjectWriters.fieldWriter("width", Image::getWidth)
        ));

        JSON.register(Image.class, ObjectReaders.of(
                Image::new,
                fieldReaderInt("height", Image::setHeight),
                fieldReader("size", Size.class, Image::setSize),
                fieldReaderString("title", Image::setTitle),
                fieldReaderString("uri", Image::setUri),
                fieldReaderInt("width", Image::setWidth)
        ));
    }

    public static class MediaContent
            implements java.io.Serializable {
        public Media media;
        public List<Image> images;

        public MediaContent() {
        }

        public void setMedia(Media media) {
            this.media = media;
        }

        public void setImages(List<Image> images) {
            this.images = images;
        }

        public Media getMedia() {
            return media;
        }

        public List<Image> getImages() {
            return images;
        }
    }

    public enum Size {
        SMALL, LARGE
    }

    public static class Image
            implements java.io.Serializable {
        private int height;
        private Size size;
        private String title;
        private String uri;
        private int width;

        public Image() {
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public String getUri() {
            return uri;
        }

        public String getTitle() {
            return title;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Size getSize() {
            return size;
        }
    }

    public enum Player {
        JAVA, FLASH
    }

    public static class Media
            implements java.io.Serializable {
        private int bitrate;   // Can be unset.
        private long duration;
        private String format;
        private int height;
        private List<String> persons;
        private Player player;
        private long size;
        private String title;
        private String uri;
        private int width;
        private String copyright;

        public Media() {
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public int getBitrate() {
            return bitrate;
        }

        public void setBitrate(int bitrate) {
            this.bitrate = bitrate;
        }

        public List<String> getPersons() {
            return persons;
        }

        public void setPersons(List<String> persons) {
            this.persons = persons;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }
    }
}
