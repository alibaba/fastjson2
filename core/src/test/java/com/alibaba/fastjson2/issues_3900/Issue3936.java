package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3936 {
    public static class JsonTest {
    }

    @Test
    public void testBaseCase() {
        String jsonStr = "{\"list\":[{\"name1\":\"ccc\"},{\"name2\":{3:[{\"id\":123}]}}]}";
        JSON.parseObject(jsonStr, JsonTest.class, JSONReader.Feature.AllowUnQuotedFieldNames);
    }

    @Test
    public void testNumericFieldNameUTF8() {
        String jsonStr = "{\"list\":[{\"name1\":\"ccc\"},{\"name2\":{3:[{\"id\":123}]}}]}";
        byte[] bytes = jsonStr.getBytes();
        JSONReader.Context context = new JSONReader.Context(JSONReader.Feature.AllowUnQuotedFieldNames);
        JSONReader reader = JSONReader.of(bytes, 0, bytes.length, context);
        JSONObject result = new JSONObject(reader.readObject());
        assertNotNull(result);
        assertNotNull(result.getJSONArray("list"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3").getJSONObject(0).get("id"));
        reader.close();
    }

    @Test
    public void testNumericFieldNameUTF16() {
        String jsonStr = "{\"list\":[{\"name1\":\"ccc\"},{\"name2\":{3:[{\"id\":123}]}}]}";
        char[] chars = jsonStr.toCharArray();
        JSONReader.Context context = new JSONReader.Context(JSONReader.Feature.AllowUnQuotedFieldNames);
        JSONReader reader = JSONReader.of(chars, 0, chars.length, context);
        JSONObject result = new JSONObject(reader.readObject());
        assertNotNull(result);
        assertNotNull(result.getJSONArray("list"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3").getJSONObject(0).get("id"));
        reader.close();
    }

    @Test
    public void testNumericFieldNameASCII() {
        String jsonStr = "{\"list\":[{\"name1\":\"ccc\"},{\"name2\":{3:[{\"id\":123}]}}]}";
        byte[] bytes = jsonStr.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        JSONReader.Context context = new JSONReader.Context(JSONReader.Feature.AllowUnQuotedFieldNames);
        JSONReader reader = JSONReader.of(bytes, 0, bytes.length, context);
        JSONObject result = new JSONObject(reader.readObject());
        assertNotNull(result);
        assertNotNull(result.getJSONArray("list"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3").getJSONObject(0).get("id"));
        reader.close();
    }
}
