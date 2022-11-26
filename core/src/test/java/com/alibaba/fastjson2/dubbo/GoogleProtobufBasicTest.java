package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GoogleProtobufBasicTest {
    @Test
    public void test() {
        GoogleProtobufBasic.GooglePBRequestType requestType = GoogleProtobufBasic.GooglePBRequestType.newBuilder()
                .setString("some string from client")
                .build();
        JSONWriter.Feature[] features = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        };
        byte[] jsonbBytes = JSONB.toBytes(
                requestType,
                features
        );
        assertNotNull(jsonbBytes);
    }
}
