package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONReader.Feature;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectReaderImplInt8ValueArrayTest {
    @Test
    public void testBase64WithJpegPrefix() {
        String base64Data = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAEBAQEBAQECAQECAQEBAQICAgICAgQDAwMDAwQEBAQEBAUFBQUFBQcHBwcHBwgICAgICAgICQkJCQkJCQkJCQn/wAALCAABAAEBAREA/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/2gAIAQEAAD8AKp//2Q==";
        String jpegBase64 = "data:image/jpeg;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + jpegBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithPngPrefix() {
        String base64Data = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=";
        String pngBase64 = "data:image/png;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + pngBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithGifPrefix() {
        String base64Data = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
        String gifBase64 = "data:image/gif;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + gifBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithWebpPrefix() {
        String base64Data = "UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA==";
        String webpBase64 = "data:image/webp;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + webpBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithBmpPrefix() {
        String base64Data = "Qk06AAAAAAAAADYAAAAoAAAAAQAAAAEAAAABABgAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String bmpBase64 = "data:image/bmp;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + bmpBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithTextPrefix() {
        String base64Data = "SGVsbG8gV29ybGQ=";
        String textBase64 = "data:text/plain;base64," + base64Data;
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + textBase64 + "\""), byte[].class, null, 0);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testBase64WithoutPrefix() {
        String base64Data = "SGVsbG8gV29ybGQ=";
        byte[] expected = Base64.getDecoder().decode(base64Data);
        ObjectReaderImplInt8ValueArray reader = new ObjectReaderImplInt8ValueArray(null);
        byte[] result = (byte[]) reader.readObject(JSONReader.of("\"" + base64Data + "\""), byte[].class, null, Feature.Base64StringAsByteArray.mask);
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }
}
