package com.alibaba.fastjson2.types;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"value\":123.45}", Bean.class);
        assertEquals(123.45D, bean.value);
    }

    @Test
    public void testJSONB() {
        byte[] jsonbBytes = JSONObject.of("value", 123.45).toJSONBBytes();
        Bean bean = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(123.45D, bean.value);
    }

    @Test
    public void test1() throws Exception {
        ObjectReader<Bean1> objectReader = ObjectReaders.objectReader(
                Bean1.class,
                Bean1::new,
                ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean1.class.getField("value"))
        );

        byte[] jsonbBytes = JSONObject.of("value", 123.45).toJSONBBytes();
        Bean1 bean = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes));
        assertEquals(123.45D, bean.value);
    }

    public static class Bean {
        public final double value;

        public Bean(double value) {
            this.value = value;
        }
    }

    public static class Bean1 {
        public double value;
    }

    @Test
    public void test2() throws Exception {
        ObjectReader<Bean2> objectReader = ObjectReaders.objectReader(
                Bean2.class,
                Bean2::new,
                ObjectReaderCreator.INSTANCE.createFieldReader("value", Bean2.class.getMethod("setValue", double.class))
        );

        {
            String str = JSONObject.of("value", 123.45).toJSONString();
            Bean2 bean = objectReader.readObject(JSONReader.of(str));
            assertEquals(123.45D, bean.value);
        }

        byte[] jsonbBytes = JSONObject.of("value", 123.45).toJSONBBytes();
        Bean2 bean = objectReader.readObject(JSONReader.ofJSONB(jsonbBytes));
        assertEquals(123.45D, bean.value);
    }

    public static class Bean2 {
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }
}
