package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBDumTest {
    @Test
    public void test_0() throws Exception {
        Int1 vo = new Int1();

        byte[] jsonbBytes = JSONB.toBytes(
                vo,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.IgnoreErrorGetter);

        JSONBDump.dump(jsonbBytes);

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2_vo.Int1\"\n" +
                "}", JSONB.toJSONString(jsonbBytes));
    }

    @Test
    public void test_1() throws Exception {
        Int1 vo = new Int1();

        byte[] jsonbBytes = JSONB.toBytes(
                vo,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.IgnoreErrorGetter);

        JSONBDump.dump(jsonbBytes);

        assertEquals("{}", JSONB.toJSONString(jsonbBytes));
    }
}
