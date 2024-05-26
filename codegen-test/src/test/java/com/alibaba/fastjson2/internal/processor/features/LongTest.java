package com.alibaba.fastjson2.internal.processor.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongTest {
    @Test
    public void writeNulls() {
        Bean bean = new Bean();
        assertEquals(
                "{\"value\":null}",
                JSON.toJSONString(bean, JSONWriter.Feature.WriteNulls)
        );
    }

    @JSONCompiled(debug = true)
    public static class Bean {
        public Long value;
    }
}
