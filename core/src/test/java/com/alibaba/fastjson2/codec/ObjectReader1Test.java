package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader1Test {
    @Test
    public void test_array() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Integer1> objectReader = creator.createObjectReader(Integer1.class);

            JSONReader jsonReader = JSONReader.of("[101]");
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            Integer1 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }

    @Test
    public void test_array_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        byte[] jsonbBytes = JSONB.toBytes(new Object[]{101});

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Integer1> objectReader = creator.createObjectReader(Integer1.class);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            Integer1 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }

    @Test
    public void test_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Integer1> objectReader = creator.createObjectReader(Integer1.class);
            Integer1 vo = objectReader.readObject(JSONReader.of("{\"v0000\":101,\"v0001\":101}"), 0);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }

    @Test
    public void test_rest_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        Map map = new HashMap<>();
        map.put("v0000", 101);
        map.put("v0001", 102);
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Integer1> objectReader = creator.createObjectReader(Integer1.class);
            Integer1 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }

    @Test
    public void test_rest_lower() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Integer1> objectReader = creator.createObjectReader(Integer1.class);

            Integer1 vo = objectReader.readObject(
                    JSONReader.of("{\"V0000\":101,\"V0001\":102}"), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Integer1 vo = new Integer1();

            JSONPath path = JSONPath.of("$.v0000");
            path.setReaderContext(
                    new JSONReader.Context(
                            new ObjectReaderProvider(creator)));
            path.set(vo, 101);
            assertEquals(Integer.valueOf(101), vo.getV0000());
        }
    }
}
