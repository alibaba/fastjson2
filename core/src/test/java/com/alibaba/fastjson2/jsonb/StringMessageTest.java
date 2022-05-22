package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

public class StringMessageTest {
    static final JSONWriter.Feature[] READER_FEATURES = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    static final JSONReader.Feature[] WRITER_FEATURES = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    @Test
    public void test() {
        StringMessage test = new StringMessage("Test");
        byte[] bytes = JSONB.toBytes(test, READER_FEATURES);
        JSONB.parseObject(bytes, Object.class, WRITER_FEATURES);
    }

    static class StringMessage
            implements Serializable {
        private static final long serialVersionUID = 7193122183120113947L;

        private String mText;

        StringMessage(String msg) {
            mText = msg;
        }

        public String toString() {
            return mText;
        }
    }
}
