package com.alibaba.fastjson2.issues_2600;

import com.alibaba.fastjson2.JSONValidator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2692 {
    @Test
    public void testWithUTF16() {
        assertTrue(JSONValidator.from("{\"你好\":[\"text\"]}").validate());
        assertFalse(JSONValidator.from("{\"你好\":[\"text\",]}").validate());
        assertFalse(JSONValidator.from("{\"你好\":[\"text\"],}").validate());
        assertFalse(JSONValidator.from("{\"你好\":[\"text\"]},").validate());
    }

    @Test
    public void testWithUTF8() {
        assertTrue(JSONValidator.fromUtf8("{\"你好\":[\"text\"]}".getBytes(StandardCharsets.UTF_8)).validate());
        assertFalse(JSONValidator.fromUtf8("{\"你好\":[\"text\",]}".getBytes(StandardCharsets.UTF_8)).validate());
        assertFalse(JSONValidator.fromUtf8("{\"你好\":[\"text\"],}".getBytes(StandardCharsets.UTF_8)).validate());
        assertFalse(JSONValidator.fromUtf8("{\"你好\":[\"text\"]},".getBytes(StandardCharsets.UTF_8)).validate());
    }
}
