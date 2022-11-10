package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ParserConfigTest {
    @Test
    public void test() {
        ParserConfig config = ParserConfig.global;
        assertEquals(ObjectReaderProvider.SAFE_MODE, config.isSafeMode());
        assertFalse(config.isAutoTypeSupport());
        config.checkAutoType(JSONObject.class);
    }
}
