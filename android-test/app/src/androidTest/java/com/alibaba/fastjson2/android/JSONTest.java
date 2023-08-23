package com.alibaba.fastjson2.android;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.android.eishay.MediaContent;
import com.alibaba.fastjson2.util.JDKUtils;

public class JSONTest {
    final static String str = "{\"images\":[{\"height\":768,\"size\":\"LARGE\",\"title\":\"Javaone Keynote\",\"uri\":\"http://javaone.com/keynote_large.jpg\",\"width\":1024},{\"height\":240,\"size\":\"SMALL\",\"title\":\"Javaone Keynote\",\"uri\":\"http://javaone.com/keynote_small.jpg\",\"width\":320}],\"media\":{\"bitrate\":262144,\"duration\":18000000,\"format\":\"video/mpg4\",\"height\":480,\"persons\":[\"Bill Gates\",\"Steve Jobs\"],\"player\":\"JAVA\",\"size\":58982400,\"title\":\"Javaone Keynote\",\"uri\":\"http://javaone.com/keynote.mpg\",\"width\":640}}";

    @Test
    public void test_parseObject() {
        User user = JSON.parseObject(
            "{\"id\":1,\"name\":\"kraity\"}", User.class
        );

        assertEquals(1, user.id);
        assertEquals("kraity", user.name);
        System.out.println(JSON.VERSION);
    }

    public static void fastjson2_perf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1; ++i) {
            JSON.parseObject(str, MediaContent.class);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("millis : " + millis);
        // zulu11.52.13 : 535 490 474
        // zulu17.32.13 : 500 485
        // zulu8.58.0.13 : 517
    }

    @Test
    public void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    static class User {
        public int id;
        public String name;
    }

}
