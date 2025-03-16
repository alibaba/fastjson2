package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullAsDefaultValueTest {
    @Test
    public void int64() {
        assertEquals("{}",
                JSONB.toJSONString(
                        JSONB.toBytes(new ByteValue1(), JSONWriter.Feature.NotWriteDefaultValue)
                ));

        assertEquals("{}",
                JSONB.toJSONString(
                        JSONB.toBytes(new ShortValue1(), JSONWriter.Feature.NotWriteDefaultValue)
                ));

        assertEquals("{}",
                JSONB.toJSONString(
                        JSONB.toBytes(new Int1(), JSONWriter.Feature.NotWriteDefaultValue)
                ));

        assertEquals("{}",
                JSONB.toJSONString(
                        JSONB.toBytes(new LongValue1(), JSONWriter.Feature.NotWriteDefaultValue)
                ));

        assertEquals("{}",
                JSONB.toJSONString(
                        JSONB.toBytes(new BooleanValue1(), JSONWriter.Feature.NotWriteDefaultValue)
                ));
    }
}
