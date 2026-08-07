package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringBuilderFieldTest {
    @Test
    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        ParserConfig config = new ParserConfig();
        config.setAsmEnable(false);

        V0 v1 = JSON.parseObject(text, V0.class, config, JSON.DEFAULT_PARSER_FEATURE);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty);
        assertEquals("{\"value\":\"\"}", text);
    }

    @Test
    public void test_deserialize_1() throws Exception {
        String json = "{\"value\":\"\"}";

        V0 vo = JSON.parseObject(json, V0.class);
        assertNotNull(vo.getValue());
        assertEquals("", vo.getValue().toString());
    }

    @Test
    public void test_deserialize_2() throws Exception {
        String json = "{\"value\":null}";

        V0 vo = JSON.parseObject(json, V0.class);
        assertNull(vo.getValue());
    }

    @Test
    public void test_deserialize_3() throws Exception {
        String json = "{\"value\":\"true\"}";

        V0 vo = JSON.parseObject(json, V0.class);
        assertNotNull(vo.getValue());
        assertEquals("true", vo.getValue().toString());
    }

    @Test
    public void test_deserialize_4() throws Exception {
        String json = "{\"value\":\"123\"}";

        V0 vo = JSON.parseObject(json, V0.class);
        assertNotNull(vo.getValue());
        assertEquals("123", vo.getValue().toString());
    }

    public static class V0 {
        private StringBuilder value;

        public StringBuilder getValue() {
            return value;
        }

        public void setValue(StringBuilder value) {
            this.value = value;
        }
    }
}
