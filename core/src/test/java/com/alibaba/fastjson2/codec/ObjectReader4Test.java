package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.StringField4;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader4Test {
    @Test
    public void test_array() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            JSONReader jsonReader = JSONReader.of("[101,102,103,104]");
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            StringField4 vo = objectReader.readObject(jsonReader, 0);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
            assertEquals("104", vo.v0003);
        }
    }

    @Test
    public void test_array_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        byte[] jsonbBytes = JSONB.toBytes(new Object[]{101, 102L, 103, 104});

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            StringField4 vo = objectReader.readObject(jsonReader, 0);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
            assertEquals("104", vo.v0003);
        }
    }

    @Test
    public void test_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            StringField4 vo = objectReader.readObject(
                    JSONReader.of("{\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":\"104\",\"x1\":true}"), 0);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
        }
    }

    @Test
    public void test_lower_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            StringField4 vo = objectReader.readObject(
                    JSONReader.of("{\"V0000\":101,\"V0001\":102,\"V0002\":103,\"V0003\":\"104\",\"x1\":true}"), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
            assertEquals("104", vo.v0003);
        }
    }

    @Test
    public void test_jsonb_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        Map map = new HashMap<>();
        map.put("v0000", 101);
        map.put("v0001", 102);
        map.put("v0002", 103);
        map.put("v0003", "104");
        map.put("x0001", "中");
        map.put("x0002", "中2");
        map.put("x0003", "中3");
        map.put("x0004", "中4");
        map.put("x0005", "中华人民共和国");
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            StringField4 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
            assertEquals("104", vo.v0003);
        }
    }

    @Test
    public void test_lower_jsonb_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        Map map = new HashMap<>();
        map.put("V0000", 101);
        map.put("V0001", 102);
        map.put("V0002", 103);
        map.put("V0003", "104");
        map.put("x0001", (byte) 0);
        map.put("x0002", 256 * 256 * 128);
        map.put("x0003", new byte[]{});
        map.put("x0004", new byte[]{1});
        map.put("x0005", new byte[]{2});
        map.put("x0006", new byte[]{3});
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<StringField4> objectReader = creator.createObjectReader(StringField4.class);

            StringField4 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals("101", vo.v0000);
            assertEquals("102", vo.v0001);
            assertEquals("103", vo.v0002);
            assertEquals("104", vo.v0003);
        }
    }
}
