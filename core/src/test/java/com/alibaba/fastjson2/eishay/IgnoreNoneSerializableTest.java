package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreNoneSerializableTest {
    @Test
    public void test() {
        URL resource = this.getClass().getClassLoader().getResource("data/eishay.json");
        MediaContent object = JSON.parseObject(resource, MediaContent.class);
        byte[] jsonbBytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.IgnoreNoneSerializable,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        assertEquals(jsonbDump, new JSONBDump(jsonbBytes, true).toString());

        Object object2 = JSONB.parseObject(
                jsonbBytes,
                Object.class, JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.IgnoreNoneSerializable,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        assertEquals(
                JSON.toJSONString(object),
                JSON.toJSONString(object2)
        );
    }

    static final String jsonbDump = "{\n" +
            "\t\"@type\":\"com.alibaba.fastjson2.eishay.vo.MediaContent#0\",\n" +
            "\t\"@value\":{\n" +
            "\t\t\"images#1\":[\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"height#2\":768,\n" +
            "\t\t\t\t\"size#3\":1,\n" +
            "\t\t\t\t\"title#4\":\"Javaone Keynote\",\n" +
            "\t\t\t\t\"uri#5\":\"http://javaone.com/keynote_large.jpg\",\n" +
            "\t\t\t\t\"width#6\":1024\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"#2\":240,\n" +
            "\t\t\t\t\"#3\":0,\n" +
            "\t\t\t\t\"#4\":\"Javaone Keynote\",\n" +
            "\t\t\t\t\"#5\":\"http://javaone.com/keynote_small.jpg\",\n" +
            "\t\t\t\t\"#6\":320\n" +
            "\t\t\t}\n" +
            "\t\t],\n" +
            "\t\t\"media#7\":{\n" +
            "\t\t\t\"bitrate#8\":262144,\n" +
            "\t\t\t\"duration#9\":18000000,\n" +
            "\t\t\t\"format#10\":\"video/mpg4\",\n" +
            "\t\t\t\"#2\":480,\n" +
            "\t\t\t\"persons#11\":[\n" +
            "\t\t\t\t\"Bill Gates\",\n" +
            "\t\t\t\t\"Steve Jobs\"\n" +
            "\t\t\t],\n" +
            "\t\t\t\"player#12\":0,\n" +
            "\t\t\t\"#3\":58982400,\n" +
            "\t\t\t\"#4\":\"Javaone Keynote\",\n" +
            "\t\t\t\"#5\":\"http://javaone.com/keynote.mpg\",\n" +
            "\t\t\t\"#6\":640\n" +
            "\t\t}\n" +
            "\t}\n" +
            "}";
}
