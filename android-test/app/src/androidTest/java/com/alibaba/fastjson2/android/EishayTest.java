package com.alibaba.fastjson2.android;

import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.android.eishay.MediaContent;

import org.junit.Test;

public class EishayTest {
    static final String str = "{\"images\": [{\n" +
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

    static int LOOP_COUNT = 1000;

    @Test
    public void eishay() {
        Log.d("fastjson", "fastjson version :" + JSON.VERSION);

        MediaContent mediaContent = JSON.parseObject(str, MediaContent.class);

        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSON.toJSONString(mediaContent);
            }
            long millis = System.currentTimeMillis() - start;
            Log.d("fastjson", "fastjson2 eishay toJSONString time : " + millis);
        }
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                JSON.parseObject(str, MediaContent.class);
            }
            long millis = System.currentTimeMillis() - start;
            Log.d("fastjson", "fastjson2 eishay parseObject time : " + millis);
        }
    }

    @Test
    public void eishay_fastjson1x() {
        Log.d("fastjson", "fastjson version : " + com.alibaba.fastjson.JSON.VERSION);

        MediaContent mediaContent = JSON.parseObject(str, MediaContent.class);

        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                com.alibaba.fastjson.JSON.toJSONString(mediaContent);
            }
            long millis = System.currentTimeMillis() - start;
            Log.d("fastjson", "fastjson1 eishay toJSONString time : " + millis);
        }
        for (int j = 0; j < 5; ++j) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP_COUNT; i++) {
                com.alibaba.fastjson.JSON.parseObject(str, MediaContent.class);
            }
            long millis = System.currentTimeMillis() - start;
            Log.d("fastjson", "fastjson1 eishay parseObject time : " + millis);
        }
    }
}
