package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Base64PrefixTest {
    @Test
    public void testBase64PrefixCompatibility() {
        // Test JPEG prefix
        String jpegBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAEBAQEBAQECAQECAQEBAQICAgICAgQDAwMDAwQEBAQEBAUFBQUFBQcHBwcHBwgICAgICAgICQkJCQkJCQkJCQn/wAALCAABAAEBAREA/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/2gAIAQEAAD8AKp//2Q==";
        byte[] jpegBytes = JSON.parseObject("{\"data\":\"" + jpegBase64 + "\"}", Data.class).data;
        assertNotNull(jpegBytes);

        // Test PNG prefix
        String pngBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=";
        byte[] pngBytes = JSON.parseObject("{\"data\":\"" + pngBase64 + "\"}", Data.class).data;
        assertNotNull(pngBytes);

        // Test GIF prefix
        String gifBase64 = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
        byte[] gifBytes = JSON.parseObject("{\"data\":\"" + gifBase64 + "\"}", Data.class).data;
        assertNotNull(gifBytes);

        // Test WEBP prefix
        String webpBase64 = "data:image/webp;base64,UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA==";
        byte[] webpBytes = JSON.parseObject("{\"data\":\"" + webpBase64 + "\"}", Data.class).data;
        assertNotNull(webpBytes);

        // Test BMP prefix
        String bmpBase64 = "data:image/bmp;base64,Qk06AAAAAAAAADYAAAAoAAAAAQAAAAEAAAABABgAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        byte[] bmpBytes = JSON.parseObject("{\"data\":\"" + bmpBase64 + "\"}", Data.class).data;
        assertNotNull(bmpBytes);

        // Test text prefix
        String textBase64 = "data:text/plain;base64,SGVsbG8gV29ybGQ=";
        byte[] textBytes = JSON.parseObject("{\"data\":\"" + textBase64 + "\"}", Data.class).data;
        assertNotNull(textBytes);
        assertArrayEquals("Hello World".getBytes(), textBytes);
    }

    public static class Data {
        public byte[] data;
    }
}
