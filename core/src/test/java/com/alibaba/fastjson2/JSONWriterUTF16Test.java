package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

public class JSONWriterUTF16Test {
    @Test
    public void test_write() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeBool(true);
        }
    }

    @Test
    public void test_writeNameRaw() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'});
        }
    }

    @Test
    public void test_writeNameRaw_1() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'}, 0, 1);
        }
    }
}
