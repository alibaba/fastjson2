package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2_vo.Boolean1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanTest2 {
    private Boolean[] values = new Boolean[]{
            null, true, false
    };

    public BooleanTest2() {
    }

    @Test
    public void test_jsonb() {
        for (Boolean id : values) {
            Boolean1 vo = new Boolean1();
            vo.setV0000(id);
            byte[] jsonbBooleans = JSONB.toBytes(vo);

            Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_jsonb_num_0() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", 0));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_num_1() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", 1));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_0() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "0"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_0_bytes() {
        byte[] jsonbBooleans = {-90, 121, 5, 118, 48, 48, 48, 48, 124, 2, 48, 0, -91};
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_1() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "1"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_1_bytes() {
        byte[] jsonbBooleans = {-90, 121, 5, 118, 48, 48, 48, 48, 124, 2, 49, 0, -91};
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_N() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "N"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_N_bytes() {
        byte[] jsonbBooleans = {-90, 121, 5, 118, 48, 48, 48, 48, 124, 2, 78, 0, -91};
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_Y() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "Y"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_Y_bytes() {
        byte[] jsonbBooleans = {-90, 121, 5, 118, 48, 48, 48, 48, 124, 2, 89, 0, -91};
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_false() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "false"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_FALSE() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "FALSE"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_FALSE_1() {
        byte[] jsonbBooleans = JSONB.toBytes(Collections.singletonMap("v0000", "FALSE"));
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_jsonb_str_true() {
        byte[] jsonbBooleans = {-90, 121, 5, 118, 48, 48, 48, 48, 121, 5, 70, 65, 76, 83, 69, -91};
        Boolean1 v1 = JSONB.parseObject(jsonbBooleans, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_utf8() {
        for (Boolean id : values) {
            Boolean1 vo = new Boolean1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Boolean1 v1 = JSON.parseObject(utf8Bytes, Boolean1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str() {
        for (Boolean id : values) {
            Boolean1 vo = new Boolean1();
            vo.setV0000(id);
            String str = JSON.toJSONString(vo);

            Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_str_num_0() {
        String str = "{\"v0000\":0}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_str_num_1() {
        String str = "{\"v0000\":1}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_str_str_0() {
        String str = "{\"v0000\":\"0\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_str_str_1() {
        String str = "{\"v0000\":\"1\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_str_str_N() {
        String str = "{\"v0000\":\"N\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_str_str_Y() {
        String str = "{\"v0000\":\"Y\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_str_str_true() {
        String str = "{\"v0000\":\"true\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_str_str_false() {
        String str = "{\"v0000\":\"false\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_str_str_TRUE() {
        String str = "{\"v0000\":\"TRUE\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.TRUE, v1.getV0000());
    }

    @Test
    public void test_str_str_FALSE() {
        String str = "{\"v0000\":\"FALSE\"}";
        Boolean1 v1 = JSON.parseObject(str, Boolean1.class);
        assertEquals(Boolean.FALSE, v1.getV0000());
    }

    @Test
    public void test_ascii() {
        for (Boolean id : values) {
            Boolean1 vo = new Boolean1();
            vo.setV0000(id);
            byte[] utf8Bytes = JSON.toJSONBytes(vo);

            Boolean1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, Boolean1.class);
            assertEquals(vo.getV0000(), v1.getV0000());
        }
    }

    @Test
    public void test_read_0() {
        ObjectReaderCreator[] creators = TestUtils.readerCreators();

        for (ObjectReaderCreator creator : creators) {
            ObjectReader<Boolean1> objectWriter = creator.createObjectReader(Boolean1.class);
            {
                Boolean1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":1}"), 0);
                assertEquals(Boolean.TRUE, vo.getV0000());
            }
            {
                Boolean1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":false}"), 0);
                assertEquals(Boolean.FALSE, vo.getV0000());
            }
            {
                Boolean1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":\"true\"}"), 0);
                assertEquals(Boolean.TRUE, vo.getV0000());
            }
            {
                Boolean1 vo = objectWriter.readObject(JSONReader.of("{\"v0000\":null}"), 0);
                assertEquals(null, vo.getV0000());
            }
        }
    }
}
