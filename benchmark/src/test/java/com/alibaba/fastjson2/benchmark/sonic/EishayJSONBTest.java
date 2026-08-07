package com.alibaba.fastjson2.benchmark.sonic;

import com.alibaba.fastjson2.*;

import java.util.List;

public class EishayJSONBTest {
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

        MediaContent mediaContent = JSON.parseObject(str, MediaContent.class);

        JSONWriter.Feature[] features = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.IgnoreNoneSerializable,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.WriteNameAsSymbol,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        };

        byte[] jsonbBytes = JSONB.toBytes(mediaContent, features);

        int LOOP_COUNT = 1000000;
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSONB.toBytes(mediaContent, features);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 eishay jsonb-serialize time : " + millis);
        }

        JSONReader.Feature[] readFeatures = {
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.IgnoreNoneSerializable,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        };

        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider(), readFeatures);
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSONB.parseObject(jsonbBytes, Object.class, context);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 eishay jsonb-deserialize time : " + millis);
        }
    }

    public class MediaContent
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
