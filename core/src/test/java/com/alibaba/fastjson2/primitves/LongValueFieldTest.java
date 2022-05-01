package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2_vo.LongValueField1;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongValueFieldTest {
    @Test
    public void test_arrayMapping() {
        ObjectWriterCreator[] creators = TestUtils.writerCreators();

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<LongValueField1> objectWriter = creator.createObjectWriter(LongValueField1.class);

            {
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 2;
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":2}", jsonWriter.toString());
            }
            {
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                objectWriter.write(jsonWriter, vo);
                assertEquals("{\"v0000\":1}", jsonWriter.toString());
            }
            {
                LongValueField1 vo = new LongValueField1();
                vo.v0000 = 1;
                JSONWriter jsonWriter = JSONWriter.of();
                jsonWriter.config(JSONWriter.Feature.BeanToArray);
                objectWriter.write(jsonWriter, vo);
                assertEquals("[1]", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_jsonb_0() {
        long[] values = new long[]{
                -16, 16
        };

        for (int i = 0; i < values.length; i++) {
            long val = values[i];
            LongValueField1 vo = new LongValueField1();
            vo.v0000 = val;

            byte[] bytes = JSONB.toBytes(vo);
            LongValueField1 vo1 = JSONB.parseObject(bytes, LongValueField1.class);
            assertEquals(vo.v0000, vo1.v0000);
        }
    }

    @Test
    public void test_jsonb_0_map() {
        long[] values = new long[]{
                100, 200, 500, 1000, 2000, 5000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000
        };

        for (int i = 0; i < values.length; i++) {
            long val = values[i];
            Map map = new HashMap();
            map.put("val", val);

            byte[] bytes = JSONB.toBytes(map);
            Map vo1 = JSONB.parseObject(bytes, Map.class);
            assertEquals(map.get("val"), vo1.get("val"));
        }
    }

    @Test
    public void test_jsonb() {
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            LongValueField1 vo = new LongValueField1();
            vo.v0000 = i;

            byte[] bytes = JSONB.toBytes(vo);
            LongValueField1 vo1 = JSONB.parseObject(bytes, LongValueField1.class);
            assertEquals(vo.v0000, vo1.v0000);
        }
    }

    @Test
    public void test_jsonb_map() {
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; ++i) {
            long val = i;
            Map map = new HashMap();
            map.put("val", val);

            byte[] bytes = JSONB.toBytes(map);
            Map vo1 = JSONB.parseObject(bytes, Map.class);
            assertEquals(map.get("val"), vo1.get("val"));
        }
    }
}
