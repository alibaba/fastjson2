package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CollectionCodecTest {
    @Test
    public void deserialze() {
        Object array = CollectionCodec.instance.deserialze(new DefaultJSONParser("[]"), null, null);
        assertNotNull(array);
        assertEquals(0, CollectionCodec.instance.getFeatures());
    }

    @Test
    public void write() throws Exception {
        JSONSerializer serializer = new JSONSerializer();
        CollectionCodec.instance.write(serializer, new JSONArray(), null, null, 0);
        assertEquals("[]", serializer.out.toString());
    }
}
