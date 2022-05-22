package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2_vo.Integer1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Int32_0 {
    static int MIN_VALUE = Short.MIN_VALUE;
    static int MAX_VALUE = Short.MAX_VALUE;

    @Test
    public void test_reflect() throws Exception {
        ObjectWriter<Integer1> ow = ObjectWriters.ofReflect(Integer1.class);
        ObjectReader<Integer1> oc = ObjectReaders.ofReflect(Integer1.class);

        for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
            Integer1 vo = new Integer1();
            vo.setV0000(i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Integer1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_lambda() throws Exception {
        ObjectWriter<Integer1> ow = ObjectWriters.objectWriter(Integer1.class);
        ObjectReader<Integer1> oc = ObjectReaders.of(Integer1.class);

        for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
            Integer1 vo = new Integer1();
            vo.setV0000(i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Integer1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }

    @Test
    public void test_manual() throws Exception {
        ObjectWriter<Integer1> ow = ObjectWriters.objectWriter(ObjectWriters.fieldWriter("v0000", Integer1::getV0000));
        ObjectReader<Integer1> oc = ObjectReaders.of(Integer1.class);

        for (int i = MIN_VALUE; i <= MAX_VALUE; ++i) {
            Integer1 vo = new Integer1();
            vo.setV0000(i);
            JSONWriter w = JSONWriter.of();
            ow.write(w, vo);

            String json = w.toString();
            JSONReader jr = JSONReader.of(json);
            Integer1 o = oc.readObject(jr, 0);
            assertEquals(vo.getV0000(), o.getV0000());
        }
    }
}
