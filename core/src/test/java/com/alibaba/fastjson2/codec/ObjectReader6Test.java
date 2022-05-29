package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.LongValueField6;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectReader6Test {
    @Test
    public void test_array() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            JSONReader jsonReader = JSONReader.of("[101,102,103,104,\"105\",106]");
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            LongValueField6 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
        }
    }

    @Test
    public void test_array_jsonb() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        byte[] jsonbBytes = JSONB.toBytes(new Object[]{101, 102L, 103, 104, "105", 106});

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            jsonReader.getContext().config(JSONReader.Feature.SupportArrayToBean);
            LongValueField6 vo = objectReader.readObject(jsonReader, 0);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
        }
    }

    @Test
    public void test_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            LongValueField6 vo = objectReader.readObject(
                    JSONReader.of("{\"v0000\":101,\"v0001\":102,\"v0002\":103,\"v0003\":\"104\",\"v0004\":\"105\",\"v0005\":\"106\",\"x1\":true}"), 0);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
        }
    }

    @Test
    public void test_lower_rest() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators2();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            LongValueField6 vo = objectReader.readObject(
                    JSONReader.of("{\"V0000\":101,\"V0001\":102,\"V0002\":103,\"V0003\":\"104\",\"v0004\":\"105\",\"v0005\":\"106\",\"x1\":true}"), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
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
        map.put("v0004", "105");
        map.put("v0005", 106L);
        map.put("x0001", LocalDateTime.now());
        map.put("x0002", "中2");
        map.put("x0003", "中3");
        map.put("x0004", "中4");
        map.put("x0005", "中华人民共和国");
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            LongValueField6 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), 0);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
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
        map.put("V0004", 105L);
        map.put("V0005", 106);
        map.put("x0001", ZonedDateTime.now());
        map.put("x0002", new Date());
        map.put("x0003", UUID.randomUUID());
        map.put("x0004", new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        map.put("x0005", false);
        map.put("x0006", true);
        byte[] jsonbBytes = JSONB.toBytes(map);

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<LongValueField6> objectReader = creator.createObjectReader(LongValueField6.class);

            LongValueField6 vo = objectReader.readObject(
                    JSONReader.ofJSONB(jsonbBytes), JSONReader.Feature.SupportSmartMatch.mask);
            assertEquals(101L, vo.v0000);
            assertEquals(102L, vo.v0001);
            assertEquals(103L, vo.v0002);
            assertEquals(104L, vo.v0003);
            assertEquals(105L, vo.v0004);
            assertEquals(106L, vo.v0005);
        }
    }
}
