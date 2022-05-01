package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.String3;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader3Test {
    @Test
    public void test_array() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            JSONReader jsonReader = JSONReader.of("[101,102,103]");
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            String3 vo = objectReader.readObject(jsonReader, 0);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
        }
    }

    @Test
    public void test_array_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        byte[] jsonbBytes = JSONB.toBytes(new Object[]{101, 102L, 103});

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            String3 vo = objectReader.readObject(jsonReader, 0);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
        }
    }

    @Test
    public void test_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            String3 vo = objectReader.readObject(
                    JSONReader.of("{\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":\"104\"}"), 0);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
        }
    }

    @Test
    public void test_lower_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            String3 vo = objectReader.readObject(
                    JSONReader.of("{\"V0000\":101,\"V0001\":102,\"V0002\":103,\"V0003\":\"104\"}"), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
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
        map.put("V0004", "x");
        map.put("V0005", "xx");
        map.put("V0006", "xxx");
        map.put("V0007", "xxxx");
        map.put("V0008", "");
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            String3 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
        }
    }

    @Test
    public void test_lower_jsonb_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        Map map = new HashMap<>();
        map.put("V0000", 101);
        map.put("V0001", 102);
        map.put("V0002", 103);
        map.put("V0003", 104F);
        map.put("V0004", 104D);
        map.put("V0005", Collections.emptyList());
        map.put("V0006", Collections.emptyMap());
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<String3> objectReader = creator.createObjectReader(String3.class);

            String3 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals("101", vo.getV0000());
            assertEquals("102", vo.getV0001());
            assertEquals("103", vo.getV0002());
        }
    }
}
