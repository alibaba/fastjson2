package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int64ValueArrayTest {
    @Test
    public void test_writeNull() {
        assertEquals("{\"values\":null}",
                JSON.toJSONString(new VO(), JSONWriter.Feature.WriteNulls));
        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO(), JSONWriter.Feature.WriteNulls)));

        assertEquals("{\"values\":null}",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONB.toBytes(new VO(), JSONWriter.Feature.WriteNulls)),
                        JSONWriter.Feature.WriteNulls
                )
        );
    }

    @Test
    public void test_writeNull2() {
        assertEquals("{}",
                JSON.toJSONString(new VO2()));

        assertEquals("{\"values\":null}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls)));

        assertEquals("{\"values\":[]}",
                new String(
                        JSON.toJSONBytes(new VO2(), JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue)));

        assertEquals("{\"values\":null}",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONB.toBytes(new VO2(), JSONWriter.Feature.WriteNulls)),
                        JSONWriter.Feature.WriteNulls)
        );

        assertEquals("{\"values\":[]}",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONB.toBytes(new VO2(), JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue)),
                        JSONWriter.Feature.WriteNulls)
        );
    }

    @Test
    public void test_1() {
        VO vo = new VO();
        vo.values = new long[]{1, 2, 3};
        assertEquals("{\"values\":[1,2,3]}",
                JSON.toJSONString(vo));

        assertEquals("{\"values\":[1,2,3]}",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONB.toBytes(vo))));
    }

    @Test
    public void test_1_setter() {
        VO2 vo = new VO2();
        vo.values = new long[]{1, 2, 3, 123, 1234, 12345, 123456, 1234567, 12345678, 123456789, 1234567890, 12345678901L, 123456789012L};
        assertEquals("{\"values\":[1,2,3,123,1234,12345,123456,1234567,12345678,123456789,1234567890,12345678901,123456789012]}",
                JSON.toJSONString(vo));

        assertEquals("{\"values\":[1,2,3,123,1234,12345,123456,1234567,12345678,123456789,1234567890,12345678901,123456789012]}",
                JSON.toJSONString(
                        JSONB.parseObject(
                                JSONB.toBytes(vo))));
    }

    public static class VO {
        public long[] values;
    }

    public static class VO2 {
        private long[] values;

        public long[] getValues() {
            return values;
        }

        public void setValues(long[] values) {
            this.values = values;
        }
    }
}
