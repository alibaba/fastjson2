package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectWriter2Test {
    @Test
    public void testAdapter() {
        ObjectWriterAdapter writerAdapter = new ObjectWriterAdapter(
                Bean.class,
                Arrays.asList(
                        ObjectWriters.fieldWriter("f0", Bean::getF0),
                        ObjectWriters.fieldWriter("f1", Bean::getF1)
                )
        );
        assertEquals(2, writerAdapter.getFieldWriters().size());
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);

        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals(str, jsonObject.toString());

        assertEquals(bean.f0, JSONPath.eval(bean, "$.f0"));
        assertEquals(bean.f1, JSONPath.eval(bean, "$.f1"));
        assertNull(JSONPath.eval(bean, "$.f100"));
    }

    @Test
    public void testJsonb() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
    }

    @Test
    public void testJsonbArray() {
        Bean bean = new Bean();
        bean.f0 = 10;
        bean.f1 = 11;
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(bean.f0, bean1.f0);
        assertEquals(bean.f1, bean1.f1);
    }

    static class Bean {
        private int f0;
        private int f1;

        public int getF0() {
            return f0;
        }

        public void setF0(int f0) {
            this.f0 = f0;
        }

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }
    }
}
