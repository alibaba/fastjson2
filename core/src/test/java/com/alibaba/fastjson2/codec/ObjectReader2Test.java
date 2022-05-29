package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2_vo.Long2;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader2Test {
    @Test
    public void test_array() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            JSONReader jsonReader = JSONReader.of("[101,102]");
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            Long2 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_array_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        byte[] jsonbBytes = JSONB.toBytes(new Object[]{101, 102L});

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            Long2 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            Long2 vo = objectReader.readObject(JSONReader.of("{\"v0000\":101,\"v0001\":102,\"v0002\":103}"), 0);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_lower_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            Long2 vo = objectReader.readObject(
                    JSONReader.of("{\"V0000\":101,\"V0001\":102,\"V0002\":103}"), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_jsonb_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        Map map = new HashMap<>();
        map.put("v0000", 101);
        map.put("v0001", 102);
        map.put("v0002", 103);
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            Long2 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_lower_jsonb_rest() {
        ObjectReaderCreator[] creators = new ObjectReaderCreator[]{
                ObjectReaderCreator.INSTANCE,
//                ObjectReaderCreatorLambda.INSTANCE,
//                ObjectReaderCreatorASM.INSTANCE
        };

        Map map = new HashMap<>();
        map.put("V0000", 101);
        map.put("V0001", 102);
        map.put("V0002", 103);
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Long2> objectReader = creator.createObjectReader(Long2.class);

            Long2 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }

    @Test
    public void test_jsonpath() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            Long2 vo = new Long2();

            JSONReader.Context readContext
                    = new JSONReader.Context(new ObjectReaderProvider(creator));
            JSONPath
                    .of("$.v0000")
                    .setReaderContext(readContext)
                    .set(vo, 101);
            JSONPath
                    .of("$.v0001")
                    .setReaderContext(readContext)
                    .set(vo, 102);
            assertEquals(Long.valueOf(101), vo.getV0000());
            assertEquals(Long.valueOf(102), vo.getV0001());
        }
    }
}
