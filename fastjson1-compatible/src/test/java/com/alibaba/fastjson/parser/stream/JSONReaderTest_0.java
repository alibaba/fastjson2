package com.alibaba.fastjson.parser.stream;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONReaderTest_0 {
    @Test
    public void test_read() throws Exception {
        JSONReader reader = new JSONReader(new StringReader("{}"));
        reader.config(Feature.AllowArbitraryCommas, true);

        JSONObject object = (JSONObject) reader.readObject();
        assertNotNull(object);

        reader.close();
    }
}
