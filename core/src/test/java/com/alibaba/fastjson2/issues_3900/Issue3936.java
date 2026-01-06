package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3936 {
    @Test
    public void testUnquotedNumericFieldName() {
        String jsonStr = "{\"list\":[{\"name1\":\"ccc\"},{\"name2\":{3:[{\"id\":123}]}}]}";
        JSONObject result = JSON.parseObject(jsonStr, JSONReader.Feature.AllowUnQuotedFieldNames);
        assertNotNull(result);
        assertNotNull(result.getJSONArray("list"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3"));
        assertNotNull(result.getJSONArray("list").getJSONObject(1).getJSONObject("name2").getJSONArray("3").getJSONObject(0).get("id"));
    }
}
