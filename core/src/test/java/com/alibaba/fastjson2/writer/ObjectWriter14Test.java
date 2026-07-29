package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("writer")
public class ObjectWriter14Test {
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
        bean.setF0('0');
        bean.setF1(null);
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.getF0(), bean1.getF0());
        assertEquals(bean.getF1(), bean1.getF1());
        assertEquals(String.valueOf(bean.getF1()), JSON.parseObject(str).getString("f1"));

        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals(str, jsonObject.toString());

        assertEquals(bean.getF0(), JSONPath.eval(bean, "$.f0"));
        assertEquals(bean.getF1(), JSONPath.eval(bean, "$.f1"));
        assertNull(JSONPath.eval(bean, "$.f100"));
    }

    @Test
    public void testJsonb() {
        Bean bean = new Bean();
        bean.setF0('0');
        bean.setF1(null);
        byte[] jsonbBytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.getF0(), bean1.getF0());
        assertEquals(bean.getF1(), bean1.getF1());
    }

    @Test
    public void testJsonbArray() {
        Bean bean = new Bean();
        bean.setF0('0');
        bean.setF1(null);
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertEquals(bean.getF0(), bean1.getF0());
        assertEquals(bean.getF1(), bean1.getF1());
    }

    interface BeanIf {
        default Character getF0() {
            return null;
        }

        default void setF0(Character f0) {
        }
    }

    public static class Bean implements BeanIf {
        private Character f1 = '0';

        public Character getF1() {
            return null == f1 ? '0' : f1;
        }

        public void setF1(Character f1) {
            this.f1 = f1;
        }
    }
}
