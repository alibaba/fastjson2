package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderJSONBUFTest {
    @Test
    public void test() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.startObject();
        jsonWriter.writeNameRaw(JSONB.toBytes("名字1"));
        jsonWriter.writeInt32(101);

        jsonWriter.writeNameRaw(JSONB.toBytes("名字2", Charset.forName("GB18030")));
        jsonWriter.writeInt32(2);

        jsonWriter.writeNameRaw(JSONB.toBytes("名字3", StandardCharsets.UTF_8));
        jsonWriter.writeBool(true);

        jsonWriter.writeNameRaw(JSONB.toBytes("名字4", StandardCharsets.UTF_16));
        jsonWriter.writeBool(false);

        jsonWriter.writeNameRaw(JSONB.toBytes("名字5", StandardCharsets.UTF_16LE));
        jsonWriter.writeInt32(105);

        jsonWriter.writeNameRaw(JSONB.toBytes("名字6", StandardCharsets.UTF_16BE));
        jsonWriter.writeRaw(JSONB.toBytes("106", StandardCharsets.UTF_16BE));

        jsonWriter.endObject();

        byte[] jsonbBytes = jsonWriter.getBytes();

        {
            JSONReader.Context context = JSONFactory.createReadContext();
            JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                    context,
                    jsonbBytes,
                    0,
                    jsonbBytes.length);

            JSONObject object = new JSONObject();
            jsonReader.readObject(object);
            assertEquals(101, object.get("名字1"));
            assertEquals(2, object.get("名字2"));
            assertEquals(Boolean.TRUE, object.get("名字3"));
            assertEquals(Boolean.FALSE, object.get("名字4"));
            assertEquals(105, object.get("名字5"));
            assertEquals("106", object.get("名字6"));
        }

        {
            JSONReader.Context context = JSONFactory.createReadContext();
            JSONReaderJSONB jsonReader = new JSONReaderJSONB(
                    context,
                    jsonbBytes,
                    0,
                    jsonbBytes.length);

            JSONObject object = new JSONObject();
            jsonReader.readObject(object);
            assertEquals(101, object.get("名字1"));
            assertEquals(2, object.get("名字2"));
            assertEquals(Boolean.TRUE, object.get("名字3"));
            assertEquals(Boolean.FALSE, object.get("名字4"));
            assertEquals(105, object.get("名字5"));
            assertEquals("106", object.get("名字6"));
        }
    }
}
