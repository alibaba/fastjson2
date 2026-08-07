package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongFieldTest_2_stream {
    @Test
    public void test_min() throws Exception {
        V0 v = new V0();
        v.setValue(Long.MIN_VALUE);

        String text = JSON.toJSONString(v);

        JSONReader reader = new JSONReader(new StringReader(text));
        V0 v1 = reader.readObject(V0.class);
        assertEquals(v1.getValue(), v.getValue());
        reader.close();
    }

    @Test
    public void test_max() throws Exception {
        V0 v = new V0();
        v.setValue(Long.MIN_VALUE);

        String text = JSON.toJSONString(v);

        JSONReader reader = new JSONReader(new StringReader(text));
        V0 v1 = reader.readObject(V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_min_array() throws Exception {
        V0 v = new V0();
        v.setValue(Long.MIN_VALUE);

        String text = JSON.toJSONString(v, SerializerFeature.BeanToArray);

        JSONReader reader = new JSONReader(new StringReader(text), Feature.SupportArrayToBean);
        V0 v1 = reader.readObject(V0.class);

        assertEquals(v1.getValue(), v.getValue());
    }

    @Test
    public void test_max_array() throws Exception {
        V0 v = new V0();
        v.setValue(Long.MIN_VALUE);

        String text = JSON.toJSONString(v, SerializerFeature.BeanToArray);

        JSONReader reader = new JSONReader(new StringReader(text), Feature.SupportArrayToBean);
        V0 v1 = reader.readObject(V0.class);

        assertEquals(v.getValue(), v1.getValue());
    }

    public static class V0 {
        private Long value;
        public Long getValue() {
            return value;
        }
        public void setValue(Long value) {
            this.value = value;
        }
    }
}
