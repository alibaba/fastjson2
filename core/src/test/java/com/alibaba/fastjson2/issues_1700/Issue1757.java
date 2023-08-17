package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1757 {
    @Test
    public void testUTF8() {
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", null, "k2", 2, "k3", 3));
            assertEquals(
                    "{\"k1\":null,\"k2\":2,\"k3\":3}",
                    jsonWriter.toString()
            );
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", 1, "k2", null, "k3", 3));
            assertEquals(
                    "{\"k1\":1,\"k2\":null,\"k3\":3}",
                    jsonWriter.toString()
            );
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", null, "k2", null, "k3", null));
            assertEquals(
                    "{\"k1\":null,\"k2\":null,\"k3\":null}",
                    jsonWriter.toString()
            );
        }
    }

    @Test
    public void testUTF16() {
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", null, "k2", 2, "k3", 3));
            assertEquals(
                    "{\"k1\":null,\"k2\":2,\"k3\":3}",
                    jsonWriter.toString()
            );
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", 1, "k2", null, "k3", 3));
            assertEquals(
                    "{\"k1\":1,\"k2\":null,\"k3\":3}",
                    jsonWriter.toString()
            );
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteMapNullValue);
            jsonWriter.write(JSONObject.of("k1", null, "k2", null, "k3", null));
            assertEquals(
                    "{\"k1\":null,\"k2\":null,\"k3\":null}",
                    jsonWriter.toString()
            );
        }
    }
}
