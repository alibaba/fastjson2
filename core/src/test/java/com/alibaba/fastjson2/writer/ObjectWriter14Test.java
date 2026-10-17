package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
                        ObjectWriters.fieldWriter("f1", Bean::getF1),
                        ObjectWriters.fieldWriter("f2", Bean::getF2)
                )
        );
        assertEquals(3, writerAdapter.getFieldWriters().size());
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setF0('0');
        bean.setF1(null);
        bean.setF2('A');
        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.getF0(), bean1.getF0());
        assertEquals(bean.getF1(), bean1.getF1());
        assertEquals(String.valueOf(bean.getF1()), JSON.parseObject(str).getString("f1"));

        // the getter of primitive char f2 returns the lowercase form ('a') while
        // the backing field holds 'A', so the serialized value must come from the getter
        assertEquals("a", JSON.parseObject(str).getString("f2"));

        JSONObject jsonObject = JSONObject.from(bean);
        assertEquals(str, jsonObject.toString());

        assertEquals(bean.getF0(), JSONPath.eval(bean, "$.f0"));
        assertEquals(bean.getF1(), JSONPath.eval(bean, "$.f1"));
        assertEquals(Character.valueOf('a'), JSONPath.eval(bean, "$.f2"));
        assertNull(JSONPath.eval(bean, "$.f100"));
    }

    @Test
    public void testJsonb() {
        Bean bean = new Bean();
        bean.setF0('0');
        bean.setF1(null);
        bean.setF2('A');
        byte[] jsonbBytes = JSONB.toBytes(bean);

        JSONObject jsonObject = JSONB.parseObject(jsonbBytes);
        assertNull(jsonObject.get("f0"));
        assertEquals(Character.valueOf('0'), jsonObject.get("f1"));
        assertEquals(Character.valueOf('a'), jsonObject.get("f2"));

        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertEquals(bean.getF0(), bean1.getF0());
        assertEquals(bean.getF1(), bean1.getF1());
    }

    @Test
    public void testJsonbArray() {
        Bean bean = new Bean();
        bean.setF0('0');
        bean.setF1(null);
        bean.setF2('A');
        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.BeanToArray);

        JSONArray array = JSONB.parseArray(jsonbBytes);
        assertNull(array.get(0));
        assertEquals(Character.valueOf('0'), array.get(1));
        assertEquals(Character.valueOf('a'), array.get(2));

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
        private char f2 = 'A';

        public Character getF1() {
            return null == f1 ? '0' : f1;
        }

        public void setF1(Character f1) {
            this.f1 = f1;
        }

        public char getF2() {
            return f2 >= 'A' && f2 <= 'Z' ? (char) (f2 + 32) : f2;
        }

        public void setF2(char f2) {
            this.f2 = f2;
        }
    }
}
