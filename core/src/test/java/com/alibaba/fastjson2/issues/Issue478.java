package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue478 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{a: \"test\"}", JSONReader.Feature.AllowUnQuotedFieldNames);
        assertEquals("test", object.get("a"));
    }

    @Test
    public void testStr() {
        String str = "{a: \"test\"}";
        JSONReader[] jsonReaders4 = TestUtils.createJSONReaders4(str);
        for (JSONReader jsonReader : jsonReaders4) {
            jsonReader.getContext().config(JSONReader.Feature.AllowUnQuotedFieldNames);
            JSONObject object = new JSONObject();
            jsonReader.read(object, 0);
            assertEquals("test", object.get("a"));
        }
    }
}
