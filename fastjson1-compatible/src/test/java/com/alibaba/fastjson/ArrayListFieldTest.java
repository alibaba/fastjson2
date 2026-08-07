package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayListFieldTest {
    @Test
    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue);
        assertEquals("{\"value\":null}", text);

        ParserConfig config = new ParserConfig();

        V0 v1 = JSON.parseObject(text, V0.class, config, JSON.DEFAULT_PARSER_FEATURE);

        assertEquals(v1.getValue(), v.getValue());
    }

    private static class V0 {
        private ArrayList<Object> value;

        public ArrayList<Object> getValue() {
            return value;
        }

        public void setValue(ArrayList<Object> value) {
            this.value = value;
        }
    }
}
