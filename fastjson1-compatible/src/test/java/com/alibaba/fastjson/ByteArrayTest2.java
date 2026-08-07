package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayTest2 {
    public static class CertFile {
        public String name;
        public byte[] data;
    }

    @Test
    public void test_0() throws Exception {
        CertFile file = new CertFile();
        file.name = "testname";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            sb.append("1");
        }
        file.data = sb.toString().getBytes();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSONWriter writer = new JSONWriter(new OutputStreamWriter(bos));
        writer.config(SerializerFeature.WriteClassName, true);
        writer.writeObject(file);
        writer.flush();

        byte[] data = bos.toByteArray();
        Charset charset = Charset.forName("UTF-8");
        CertFile convertFile = (CertFile) JSON.parse(data, 0, data.length, charset.newDecoder(), Feature.AllowArbitraryCommas,
                Feature.IgnoreNotMatch, Feature.SortFeidFastMatch, Feature.DisableCircularReferenceDetect,
                Feature.AutoCloseSource, Feature.SupportAutoType
        );

        assertEquals(file.name, convertFile.name);
        assertArrayEquals(file.data, convertFile.data);
    }
}
