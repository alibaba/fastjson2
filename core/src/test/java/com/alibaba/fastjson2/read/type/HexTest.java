package com.alibaba.fastjson2.read.type;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HexTest {
    @Test
    public void test() {
        String str = "abc";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);
        String json = JSON.toJSONString(hex);
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }

    @Test
    public void test1() {
        String str = "abc";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);
        String json = JSON.toJSONString(hex, JSONWriter.Feature.UseSingleQuotes);
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }

    @Test
    public void error() {
        String str = "abc";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);

        assertThrows(JSONException.class, () -> JSONReader.of(hex.toCharArray()).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex.getBytes(StandardCharsets.UTF_8)).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex).readHex());
    }

    @Test
    public void error1() {
        String hex = "\"0AC\"";
        assertThrows(JSONException.class, () -> JSONReader.of(hex.toCharArray()).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex.getBytes(StandardCharsets.UTF_8)).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex).readHex());
    }

    @Test
    public void error2() {
        String hex = "\"0K\"";
        assertThrows(JSONException.class, () -> JSONReader.of(hex.toCharArray()).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex.getBytes(StandardCharsets.UTF_8)).readHex());
        assertThrows(JSONException.class, () -> JSONReader.of(hex).readHex());
    }

    @Test
    public void empty() {
        byte[] bytes = new byte[0];
        String json = "\"\"";
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }

    @Test
    public void space() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"616263\"  ";
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }

    @Test
    public void space1() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"616263\"  ,  ";
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }

    @Test
    public void comment() {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"616263\"  ,  // ";
        assertArrayEquals(bytes, JSONReader.of(json.toCharArray()).readHex());
        assertArrayEquals(bytes, JSONReader.of(json.getBytes(StandardCharsets.UTF_8)).readHex());
        assertArrayEquals(bytes, JSONReader.of(json).readHex());
    }
}
