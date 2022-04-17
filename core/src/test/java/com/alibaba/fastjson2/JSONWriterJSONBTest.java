package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

public class JSONWriterJSONBTest {
    @Test
    public void test_startObject() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startObject();
            jsonWriter.endObject();
        }
    }

    @Test
    public void test_startArray() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.startArray(1);
        }
    }


    @Test
    public void test_writeRaw() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(JSONB.Constants.BC_NULL);
        }
    }

    @Test
    public void test_writeRaw_1() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeRaw(new byte[]{JSONB.Constants.BC_NULL});
        }
    }

    @Test
    public void test_writeMillis() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeMillis(1);
        }
    }
}
